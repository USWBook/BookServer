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
import com.example.demo.domain.report.entity.UserReport;
import com.example.demo.domain.report.enums.ReportReason;
import com.example.demo.domain.report.enums.ReportType;
import com.example.demo.domain.report.repository.UserReportRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    public static final String CHAT_ROOMS = "CHAT_ROOM"; // Redis 상위 키

    // ✅ chatRedisTemplate 주입
    @Qualifier("chatRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserReportRepository userReportRepository;

    private final UserRepository userRepository;
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

        // (없을시) 신규 채팅방 생성
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
    //유저 신고
    @Transactional
    public UserReport reportUserByRoom(UUID roomId, User reporter, ReportReason reason) {
        ChatRoom chatRoom = findChatRoomById(roomId);
        if (chatRoom == null) throw new RuntimeException("채팅방 없음");

        UUID reportedUserId = chatRoom.getSender().equals(reporter.getId())
                ? chatRoom.getReceiver()
                : chatRoom.getSender();

        User reported = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new RuntimeException("신고 대상자 없음"));

        UserReport report = UserReport.builder()
                .reporterName(reporter.getName())   // 닉네임만 저장
                //.reportedName는 엔티티에서 제거했으므로 저장 안 함
                .reportType(ReportType.CHAT)
                .reason(reason)
                .reportThingId(roomId)
                .reportedAt(LocalDateTime.now())
                .build();

        return userReportRepository.save(report);
    }


    //Redis에 저장된 채팅방 데이터를 조회
    public ChatRoom findChatRoomById(UUID roomId) {
        return hashOpsChatRoom.get(CHAT_ROOMS, roomId.toString());
    }

    //참가자 확인
    public boolean isSameParticipants(ChatRoom room, UUID userA, UUID userB) {
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

    // UUID로 채팅방 조회: 저장소(redis 등)에서 채팅방을 찾아 반환, 없으면 예외 발생
    public ChatRoom findByRoomId(UUID roomId) {
        ChatRoom room = hashOpsChatRoom.get(CHAT_ROOMS, roomId.toString());
        if (room == null) throw new ChatRoomNotFoundException();
        return room;
    }

    // 채팅방 삭제 처리: 특정 사용자가 채팅방을 삭제 처리(soft delete)하면 해당 상태 저장
    // 양쪽 사용자 모두 삭제 상태면 실제 저장소에서 완전 삭제
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

    // 채팅방 나가기: 현재는 deleteChatRoom 메서드 호출로 동일하게 처리
    public void leaveChatRoom(UUID roomId, UUID userId) {
        ChatRoom room = hashOpsChatRoom.get(CHAT_ROOMS, roomId.toString());
        if (room == null) {
            // 이미 방이 삭제됐거나 없음, 예외 대신 로그 남기고 종료하거나 무시 가능
            // throw new ChatRoomNotFoundException(); // 대신 다음 처리
            return;
        }

        // 사용자 나간 상태로 표시
        room.setDeleteStatus(userId.toString(), true);
        hashOpsChatRoom.put(CHAT_ROOMS, roomId.toString(), room);

        // 남은 유저 수 계산
        int remainingUserCount = getUserCount(roomId);

        // 0명이면 완전 삭제 처리
        if (remainingUserCount == 0) {
            hashOpsChatRoom.delete(CHAT_ROOMS, roomId.toString());
            deleteChatRoomFromDb(roomId);
        }
    }



    // 채팅방에 남아있는(삭제하지 않은) 유저 수 반환
    public int getUserCount(UUID roomId) {
        ChatRoom room = hashOpsChatRoom.get(CHAT_ROOMS, roomId.toString());
        if (room == null) return 0;

        int count = 0;
        if (!room.getDeleteStatus(room.getSender().toString())) count++;
        if (!room.getDeleteStatus(room.getReceiver().toString())) count++;

        return count;
    }


    // 채팅방에서 상대방 사용자 UUID 반환: 현재 사용자의 상대방을 찾기 위한 용도
    // 현재 사용자가 방에 없으면 예외 발생
    public UUID findOther(UUID roomId, UUID sender) {
        ChatRoom room = findByRoomId(roomId);
        if (!room.getSender().equals(sender) && !room.getReceiver().equals(sender)) {
            throw new ChatAccessDeniedException();
        }
        return room.getSender().equals(sender) ? room.getReceiver() : room.getSender();
    }

    // DB에서 채팅방 완전 삭제
    public void deleteChatRoomFromDb(UUID roomId) {
        chatRoomUserRepository.deleteByChatRoomId(roomId);
    }

    // Redis에서 채팅방 완전 삭제
    public void deleteChatRoomFromRedis(UUID roomId) {
        hashOpsChatRoom.delete(CHAT_ROOMS, roomId.toString());
    }


}
