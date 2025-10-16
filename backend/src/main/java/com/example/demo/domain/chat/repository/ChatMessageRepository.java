package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 채팅 메시지를 DB에서 관리하는 JPA Repository
 * - 기본 CRUD(저장, 조회, 삭제 등)는 JpaRepository가 자동 구현
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    //특정 채팅방(roomId)의 모든 메시지 조회
    List<ChatMessage> findByChatRoomId(UUID roomId);

    // 채팅방에서 특정 시각 이전(포함) 메시지 모두 조회
    List<ChatMessage> findByChatRoomIdAndSentAtLessThanEqual(UUID chatRoomId, LocalDateTime lastReadAt);

}
