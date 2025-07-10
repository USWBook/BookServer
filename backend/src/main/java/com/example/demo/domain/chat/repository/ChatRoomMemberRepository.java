package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    // 특정 멤버가 속한 모든 채팅방 조회
    List<ChatRoomMember> findByMember_Id(UUID memberId);

    // 특정 채팅방의 모든 멤버 조회
    List<ChatRoomMember> findByChatRoom_Id(UUID chatRoomId);

    // 특정 채팅방에 특정 멤버가 참여 중인지 확인
    boolean existsByChatRoom_IdAndMember_Id(UUID chatRoomId, UUID memberId);
}

