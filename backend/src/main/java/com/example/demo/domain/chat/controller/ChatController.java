package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.*;
import com.example.demo.domain.chat.dto.response.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // 채팅방 생성
    @PostMapping("/room")
    public ResponseEntity<CreateChatRoomResponseDto> createRoom(
            @RequestBody CreateChatRoomRequestDto request
    ) {
        return ResponseEntity.ok(
                new CreateChatRoomResponseDto(
                        200, "채팅방 생성 성공",
                        new CreateChatRoomResponseDto.Data(UUID.randomUUID(), LocalDateTime.now())
                )
        );
    }

    // 채팅방 입장
    @PostMapping("/room/enter")
    public ResponseEntity<EnterChatRoomResponseDto> enterRoom(
            @RequestParam UUID roomId
    ) {
        return ResponseEntity.ok(
                new EnterChatRoomResponseDto(
                        200, "입장 성공",
                        new EnterChatRoomResponseDto.Data(roomId, List.of())
                )
        );
    }

    // 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<ListChatRoomsResponseDto> listRooms() {
        return ResponseEntity.ok(
                new ListChatRoomsResponseDto(
                        200, "채팅방 목록",
                        List.of()
                )
        );
    }

    // 메시지 전송
    @PostMapping("/message")
    public ResponseEntity<SendMessageResponseDto> sendMessage(
            @RequestBody SendMessageRequestDto request
    ) {
        return ResponseEntity.ok(
                new SendMessageResponseDto(
                        200, "메시지 전송 성공",
                        new SendMessageResponseDto.Data(
                                UUID.randomUUID(), request.roomId(), request.senderId(), request.message(), LocalDateTime.now()
                        )
                )
        );
    }

    // 이미지 전송
    @PostMapping("/message/image")
    public ResponseEntity<SendImageResponseDto> sendImage(
            @RequestParam UUID roomId,
            @RequestParam UUID senderId,
            @RequestPart MultipartFile image
    ) {
        return ResponseEntity.ok(
                new SendImageResponseDto(
                        200, "이미지 전송 성공",
                        new SendImageResponseDto.Data(
                                UUID.randomUUID(),
                                "https://example.com/image.jpg",
                                LocalDateTime.now()
                        )
                )
        );
    }

    // 메시지 신고
    @PostMapping("/message/report")
    public ResponseEntity<ReportMessageResponseDto> reportMessage(
            @RequestBody ReportMessageRequestDto request
    ) {
        return ResponseEntity.ok(
                new ReportMessageResponseDto(200, "메시지 신고 완료")
        );
    }

    // 채팅방 나가기
    @PostMapping("/room/leave")
    public ResponseEntity<LeaveChatRoomResponseDto> leaveRoom(
            @RequestBody LeaveChatRoomRequestDto request
    ) {
        return ResponseEntity.ok(
                new LeaveChatRoomResponseDto(200, "채팅방 나가기 완료")
        );
    }

    // 채팅방 삭제
    @DeleteMapping("/room")
    public ResponseEntity<DeleteChatRoomResponseDto> deleteRoom(
            @RequestParam UUID roomId
    ) {
        return ResponseEntity.ok(
                new DeleteChatRoomResponseDto(200, "채팅방 삭제 완료")
        );
    }

    // 메시지 읽음 처리
    @PostMapping("/message/read")
    public ResponseEntity<MarkMessagesReadResponseDto> markMessagesRead(
            @RequestBody MarkMessagesReadRequestDto request
    ) {
        return ResponseEntity.ok(
                new MarkMessagesReadResponseDto(200, "메시지 읽음 처리 완료")
        );
    }

    // 유저 신고
    @PostMapping("/user/report")
    public ResponseEntity<ReportUserResponseDto> reportUser(
            @RequestBody ReportUserRequestDto request
    ) {
        return ResponseEntity.ok(
                new ReportUserResponseDto(200, "유저 신고 완료")
        );
    }

    // 알림 토큰 등록
    @PostMapping("/notification")
    public ResponseEntity<NotificationResponseDto> registerNotificationToken(
            @RequestBody NotificationRequestDto request
    ) {
        return ResponseEntity.ok(
                new NotificationResponseDto(200, "알림 토큰 등록 완료")
        );
    }
}