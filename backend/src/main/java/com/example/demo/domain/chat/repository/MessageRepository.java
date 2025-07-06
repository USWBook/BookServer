package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    // 채팅방의 메시지 내역 조회 (예: 최신순)
    List<Message> findByChatRoom_IdOrderBySentAtAsc(UUID chatRoomId);

    // 필요시 커스텀 쿼리 메소드 추가
}
