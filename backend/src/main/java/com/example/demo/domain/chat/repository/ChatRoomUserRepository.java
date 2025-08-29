package com.example.demo.domain.chat.repository;

import com.example.demo.domain.chat.entity.ChatRoomUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, UUID> {

    // 특정 멤버가 속한 모든 채팅방멤버 조회 (user 엔티티 연관관계 기준)
    List<ChatRoomUser> findByUser_Id(UUID userId);

    // 특정 채팅방에 속한 모든 멤버 조회 (chatRoomId 단순 필드 기준)
    List<ChatRoomUser> findByChatRoomId(UUID chatRoomId);

    // 특정 채팅방에 특정 멤버가 속해있는지 확인
    boolean existsByChatRoomIdAndUser_Id(UUID chatRoomId, UUID userId);

    // 특정 채팅방에 속한 모든 사용자 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatRoomUser u WHERE u.chatRoomId = :roomId")
    void deleteByChatRoomId(UUID roomId);
}
