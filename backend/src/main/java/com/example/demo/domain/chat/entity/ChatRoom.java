package com.example.demo.domain.chat.entity;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom implements Serializable {

    //이 클래스가 직렬화될 때 버전이 1인 클래스라고 표시
    private static final long serialVersionUID = 1L;

    private UUID roomId;         // 채팅방 고유 ID
    private UUID postId;         // 게시글 ID
    private String name;           // 상대방 닉네임
    private UUID sender;         // 나의 ID
    private UUID receiver;       // 상대방 ID
    private int userCount;         // 참여자 수 (1:1이면 2로 고정) 퇴장시 -1
    private String lastMessage;    // 마지막 메시지
    private String lastTimestamp; // 마지막 메시지 시간
    private LocalDateTime createdAt;

    // 사용자별 논리적 삭제 상태 저장
    @Builder.Default
    private Map<String, Boolean> isDelete = new HashMap<>();

    public ChatRoom(UUID postId, UUID sender, UUID receiver) {
        this.roomId = UUID.randomUUID(); //기존의 없던 값이니 생성
        this.postId = postId;
        this.name = null; // 상대방 닉네임 (지금은 null)
        this.sender = sender;
        this.receiver = receiver;
        this.userCount = 2;
        this.createdAt = LocalDateTime.now();
        this.isDelete = new HashMap<>();
    }

    // 논리적 삭제 상태 설정
    public void setDeleteStatus(String username, boolean status) {
        this.isDelete.put(username, status);
    }

    // 논리적 삭제 상태 조회
    public boolean getDeleteStatus(String username) {
        // 값이 없으면 기본적으로 false(삭제 안 함)로 처리
        return isDelete.getOrDefault(username, false);
    }

}
