package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    // 필요시 커스텀 쿼리 메소드 추가
}
