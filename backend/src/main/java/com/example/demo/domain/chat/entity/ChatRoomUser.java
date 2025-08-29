package com.example.demo.domain.chat.entity;

import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "chat_room_users")
public class ChatRoomUser {

    //roomId PK
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "chat_room_user_id", updatable = false, nullable = false)
    private UUID id;

    // 참여자가 어느 채팅방 소속인지 알 수 있음 FK
    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    //채팅방에 속해있는 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
