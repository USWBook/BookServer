package com.example.demo.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Transient;

/**
 * Redis/메모리 기반으로 관리하는 채팅 메시지 객체
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 메시지 고유 식별자 (UUID) */
    @Id
    private String messageId = UUID.randomUUID().toString();

    /** 메시지 타입 (입장/퇴장/일반채팅/이미지) */
    @Enumerated(EnumType.STRING)
    private MessageType type;

    /** 채팅방 고유 ID */
    private UUID roomId;

    /** 메시지 보낸 사람(사용자 ID) */
    private UUID sender;

    /** 메시지 받는 사람(상대) */
    private UUID receiver;

    /** 메시지 내용 (텍스트) */
    private String message;

    /** 이미지가 포함된 경우 이미지 URL */
    private String image;

    /** 채팅방 현재 인원수 (1:1이면 2, 퇴장 시 갱신) */
    private long userCount;

    /** 메시지 전송 시각 (HH:mm 형식) */
    private String timestamp;

    /**
     * 사용자별 논리적 삭제 상태
     * key: 사용자명(또는 ID), value: true(삭제함), false(삭제안함)
     * ex) A는 삭제했지만 B는 아직 방에 남아있는 상태 관리
     */
    @Transient
    private Map<UUID, Boolean> deleteStatus = new HashMap<>();

    @Builder
    public ChatMessage(
            MessageType type,
            UUID roomId,
            UUID sender,
            UUID receiver,
            String message,
            String image,
            long userCount
    ) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.roomId = roomId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.image = image;
        this.userCount = userCount;
        this.timestamp = formatTimestamp(LocalDateTime.now());

        // 기본값: 두 사용자 모두 삭제 안 함
        deleteStatus.put(sender, false);
        deleteStatus.put(receiver, false);
    }

    /** 메시지 타입: 입장, 퇴장, 일반채팅, 이미지 */
    public enum MessageType {
        ENTER, QUIT, TALK, IMAGE
    }

    /** 현재 시간을 HH:mm 형식 문자열로 변환 */
    public String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.format(formatter);
    }

    /** 사용자별 논리적 삭제상태 설정 */
    public void setDeleteStatus(UUID userId, boolean status) {
        this.deleteStatus.put(userId, status);
    }

    /** 사용자별 논리적 삭제상태 조회 */
    public Boolean getDeleteStatus(UUID userId) {
        return this.deleteStatus.getOrDefault(userId, false);
    }
}
