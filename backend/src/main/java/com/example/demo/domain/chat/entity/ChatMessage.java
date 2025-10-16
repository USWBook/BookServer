package com.example.demo.domain.chat.entity;

import com.example.demo.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "messages") // DB 테이블명 유지
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChatMessage {

    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private UUID id;

    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime sentAt;

    private int readCount; // 기본값 2 (1:1 채팅 기준)

    @PrePersist
    public void prePersist() {
        sentAt = LocalDateTime.now();
        readCount = 2; // 기본값
        isRead = false;
    }

    public void decreaseReadCount() {
        if (readCount > 0) readCount--;
        if (readCount == 0) isRead = true;
    }
}
