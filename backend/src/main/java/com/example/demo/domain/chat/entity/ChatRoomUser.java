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

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "chat_room_member_id", updatable = false, nullable = false)
    private UUID id;

    // JPA 연관관계 없이 단순 UUID 보관
    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
