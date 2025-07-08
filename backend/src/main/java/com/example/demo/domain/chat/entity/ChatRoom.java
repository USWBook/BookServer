package com.example.demo.domain.chat.entity;

import com.example.demo.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue
    @Column(name = "chat_room_id")
    private UUID id;

    // 게시글과 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 필요하다면 채팅방 이름 등 추가 가능
}
