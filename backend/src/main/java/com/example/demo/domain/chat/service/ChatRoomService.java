package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.entity.ChatRoomUser;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;

import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.chat.repository.ChatRoomUserRepository;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private static final String CHAT_ROOMS = "CHAT_ROOM"; // Redis 상위 키

    // ✅ chatRedisTemplate 주입
    @Qualifier("chatRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

    private final ChatRoomUserRepository chatRoomUserRepository; // 추가

    private final UserRepository userRepository; // 추가: user 엔티티 조회 용도
    private final PostRepository postRepository;

    private final ChatMessageRepository chatMessageRepository; // 메시지 삭제용 추가


    @PostConstruct
    public void init() {
        this.hashOpsChatRoom = redisTemplate.opsForHash();
    }

    // 1:1 채팅방 생성 (동일 참가자 조합 중복 방지)
    @Transactional
    public ChatRoom createOrGetRoom(CreateChatRoomRequestDto dto, UUID buyerId) {
        UUID postId = dto.postId();
        if (postId == null) throw new IllegalArgumentException("postId는 필수입니다.");

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다. postId = " + postId));

        UUID sellerId = post.getSeller().getId();
        if (sellerId == null) throw new IllegalStateException("게시글 판매자 정보가 없습니다.");

        // Redis 캐시에서 같은 구성의 채팅방 탐색
        for (ChatRoom existingRoom : hashOpsChatRoom.values(CHAT_ROOMS)) {
            if (isSameParticipants(existingRoom, buyerId, sellerId) &&
                    Objects.equals(existingRoom.getPostId(), postId)) {
                existingRoom.setDeleteStatus(buyerId.toString(), false);
                hashOpsChatRoom.put(CHAT_ROOMS, existingRoom.getRoomId().toString(), existingRoom);
                return existingRoom;
            }
        }

        // 신규 채팅방 생성
        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);

        User sellerUser = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자 유저 없음: " + sellerId));
        User buyerUser = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자 유저 없음: " + buyerId));

        ChatRoomUser sellerMember = ChatRoomUser.builder()
                .chatRoomId(chatRoom.getRoomId())
                .user(sellerUser)
                .build();
        ChatRoomUser buyerMember = ChatRoomUser.builder()
                .chatRoomId(chatRoom.getRoomId())
                .user(buyerUser)
                .build();

        chatRoomUserRepository.save(sellerMember);
        chatRoomUserRepository.save(buyerMember);

        return chatRoom;
    }



    private boolean isSameParticipants(ChatRoom room, UUID userA, UUID userB) {
        UUID sender = room.getSender();
        UUID receiver = room.getReceiver();

        if (sender == null || receiver == null) {
            throw new InvalidChatArgumentException();
        }

        return (sender.equals(userA) && receiver.equals(userB)) ||
                (sender.equals(userB) && receiver.equals(userA));
    }

    public List<ChatRoom> findRoomByUser(UUID userId) {
        List<ChatRoom> result = new ArrayList<>();
        for (ChatRoom room : hashOpsChatRoom.values(CHAT_ROOMS)) {
            if ((room.getSender().equals(userId) || room.getReceiver().equals(userId)) &&
                    !room.getDeleteStatus(userId.toString())) {
                result.add(room);
            }
        }
        return result;
    }

    public ChatRoom findByRoomId(UUID roomId) {
        ChatRoom room = hashOpsChatRoom.get(CHAT_ROOMS, roomId.toString());
        if (room == null) throw new ChatRoomNotFoundException();
        return room;
    }

    public void deleteChatRoom(UUID roomId, UUID userId) {
        ChatRoom room = findByRoomId(roomId);
        if (room != null) {
            room.setDeleteStatus(userId.toString(), true);
            hashOpsChatRoom.put(CHAT_ROOMS, roomId.toString(), room);

            if (room.getDeleteStatus(room.getSender().toString()) &&
                    room.getDeleteStatus(room.getReceiver().toString())) {
                hashOpsChatRoom.delete(CHAT_ROOMS, roomId.toString());
            }
        }
    }

    public void leaveChatRoom(UUID roomId, UUID userId) {
        deleteChatRoom(roomId, userId);
    }

    public int getUserCount(UUID roomId) {
        ChatRoom room = findByRoomId(roomId);
        if (room == null) return 0;

        int count = 0;
        if (!room.getDeleteStatus(room.getSender().toString())) count++;
        if (!room.getDeleteStatus(room.getReceiver().toString())) count++;

        return count;
    }

    public UUID findOther(UUID roomId, UUID sender) {
        ChatRoom room = findByRoomId(roomId);
        if (!room.getSender().equals(sender) && !room.getReceiver().equals(sender)) {
            throw new ChatAccessDeniedException();
        }
        return room.getSender().equals(sender) ? room.getReceiver() : room.getSender();
    }


}
