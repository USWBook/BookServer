package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.*;
import com.example.demo.domain.chat.dto.response.*;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.global.response.RsData;
import com.example.demo.global.response.Empty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping("/room")
    public RsData<CreateChatRoomResponseDto> createRoom(@RequestBody CreateChatRoomRequestDto dto) {
        ChatRoom room = chatRoomService.createOrGetRoom(dto);
        return new RsData<>("200", "채팅방 생성 완료", CreateChatRoomResponseDto.from(room));
    }

    // 내 채팅방 목록 조회
    @GetMapping("/rooms")
    public RsData<ListChatRoomsResponseDto> listRooms(@RequestParam UUID userId) {
        List<ChatRoom> rooms = chatRoomService.findRoomByUser(userId);

        // ChatRoom → ChatRoomDto로 변환
        List<ListChatRoomsResponseDto.ChatRoomDto> dtos = rooms.stream()
                .map(room -> new ListChatRoomsResponseDto.ChatRoomDto(
                        room.getRoomId().toString(),
                        room.getPostId() != null ? room.getPostId().toString() : null,
                        room.getName(),
                        room.getUserCount(),
                        room.getLastMessage(),
                        room.getLastTimestamp()
                ))
                .toList();

        // 최종 DTO 포매팅
        ListChatRoomsResponseDto responseDto =
                new ListChatRoomsResponseDto(200, "나의 채팅방 목록 조회 성공", dtos);

        return new RsData<>("200", "나의 채팅방 목록 조회 성공", responseDto);
    }


    // 채팅방 하나 조회
    @GetMapping("/room/{roomId}")
    public RsData<ChatRoom> getRoom(@PathVariable UUID roomId) {
        return Optional.ofNullable(chatRoomService.findByRoomId(roomId))
                .map(room -> new RsData<>("200", "채팅방 조회 성공", room))
                .orElseGet(() -> new RsData<>("404", "채팅방을 찾을 수 없습니다."));
    }

    // 채팅방 메시지 이력 조회
    @GetMapping("/room/{roomId}/messages")
    public RsData<List<ChatMessage>> getMessages(@PathVariable UUID roomId) {
        List<ChatMessage> messages = chatService.getMessages(roomId);
        return new RsData<>("200", "채팅 메시지 목록 조회 성공", messages);
    }

    // 이미지 전송
    @PostMapping("/room/{roomId}/image")
    public RsData<ChatMessage> sendImage(
            @PathVariable UUID roomId,
            @RequestParam UUID sender,
            @RequestParam UUID receiver,
            @RequestPart MultipartFile image
    ) {
        ChatMessage msg = chatService.sendImage(roomId, sender, receiver, image);
        return new RsData<>("200", "이미지 전송 성공", msg);
    }

    // 메시지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public RsData<Empty> markMessagesRead(@PathVariable UUID roomId, @RequestParam String username) {
        chatService.markMessagesRead(roomId, username);
        return new RsData<>("200", "메시지 읽음 처리 완료");
    }

    // 논리적 삭제 (나만 삭제)
    @DeleteMapping("/room/{roomId}")
    public RsData<String> deleteChatRoom(@PathVariable UUID roomId, @RequestParam String username) {
        chatRoomService.deleteChatRoom(roomId, username);
        return new RsData<>("200", "채팅방 삭제 처리 완료", null);
    }

    // URL string에서 roomId 추출 (예시)
    @GetMapping("/room-id")
    public RsData<UUID> getRoomId(@RequestParam String destination) {
        UUID roomId = chatService.getRoomId(destination);
        if (roomId == null)
            return new RsData<>("400", "roomId 파싱 실패", null);
        return new RsData<>("200", "roomId 추출 성공", roomId);
    }


    //채팅 메시지 전송 API
    @PostMapping("/messages")
    public RsData<Void> sendChatMessage(@RequestBody SendMessageRequestDto request) {
        // ChatMessage 객체 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.TALK)
                .roomId(request.roomId())
                .sender(request.senderId())
                .receiver(request.receiverId())
                .message(request.message())
                .build();

        // 서비스 메서드 호출
        chatService.sendChatMessage(chatMessage);

        // 글로벌 정책에 맞는 RsData 구조로 반환
        return new RsData<>("200", "채팅 메시지 전송 완료");
    }

}

