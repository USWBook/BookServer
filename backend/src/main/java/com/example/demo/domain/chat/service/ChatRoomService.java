package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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

    @PostConstruct
    public void init() {
        this.hashOpsChatRoom = redisTemplate.opsForHash();
    }

    // 1:1 채팅방 생성 (동일 참가자 조합 중복 방지)
    public ChatRoom createOrGetRoom(CreateChatRoomRequestDto dto) {
        UUID postId = dto.postId();
        UUID sellerId = dto.sellerId(); // receiver
        UUID buyerId = dto.buyerId();   // sender

        // 기존 채팅방이 있는지 Redis에서 확인
        for (ChatRoom existingRoom : hashOpsChatRoom.values(CHAT_ROOMS)) {
            if (isSameParticipants(existingRoom, buyerId, sellerId) &&
                    Objects.equals(existingRoom.getPostId(), postId)) {
                // 구매자(sender)에 대해 논리적 삭제 복구
                existingRoom.setDeleteStatus(buyerId.toString(), false);
                hashOpsChatRoom.put(CHAT_ROOMS, existingRoom.getRoomId().toString(), existingRoom);
                return existingRoom;
            }
        }

        // ❗ 없을 시 새로 생성
        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);
        return chatRoom;
    }

    private boolean isSameParticipants(ChatRoom room, UUID userA, UUID userB) {
        return (room.getSender().equals(userA) && room.getReceiver().equals(userB)) ||
                (room.getSender().equals(userB) && room.getReceiver().equals(userA));
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

    public void deleteChatRoom(UUID roomId, String username) {
        ChatRoom room = findByRoomId(roomId);
        if (room != null) {
            room.setDeleteStatus(username, true);
            hashOpsChatRoom.put(CHAT_ROOMS, roomId.toString(), room);
            if (room.getDeleteStatus(room.getSender().toString()) &&
                    room.getDeleteStatus(room.getReceiver().toString())) {
                hashOpsChatRoom.delete(CHAT_ROOMS, roomId.toString());
            }
        }
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
