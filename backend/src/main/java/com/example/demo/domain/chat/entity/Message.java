package com.example.demo.domain.chat.entity;

import com.example.demo.domain.post.entity.Member;
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
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime sentAt;
}
