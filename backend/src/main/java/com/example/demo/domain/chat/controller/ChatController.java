package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.CreateChatRoomResponseDto;
import com.example.demo.domain.chat.dto.response.ListChatRoomsResponseDto;
import com.example.demo.domain.chat.dto.response.SendMessageResponseDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.global.response.RsData;
import com.example.demo.global.response.Empty;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        List<ListChatRoomsResponseDto.ChatRoomDto> dtos = rooms.stream()
                .map(room -> new ListChatRoomsResponseDto.ChatRoomDto(
                        room.getRoomId().toString(),
                        room.getPostId() != null ? room.getPostId().toString() : null,
                        room.getName(),
                        room.getUserCount(),
                        room.getLastMessage(),
                        room.getLastTimestamp()
                ))
                .collect(Collectors.toList());

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
    public RsData<List<SendMessageResponseDto.Data>> getMessages(@PathVariable UUID roomId) {
        List<ChatMessage> messages = chatService.getMessages(roomId);

        List<SendMessageResponseDto.Data> dtos = messages.stream()
                .map(m -> new SendMessageResponseDto.Data(
                        m.getId(),
                        m.getChatRoomId(),
                        m.getSender().getId(),
                        m.getContent(),
                        m.getSentAt()
                ))
                .collect(Collectors.toList());

        return new RsData<>("200", "채팅 메시지 목록 조회 성공", dtos);
    }

    // 채팅 메시지 전송
    @PostMapping("/messages")
    public RsData<SendMessageResponseDto> sendChatMessage(@RequestBody SendMessageRequestDto request) {
        ChatMessage savedMessage = chatService.sendChatMessage(request);

        SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        SendMessageResponseDto responseDto = new SendMessageResponseDto(200, "채팅 메시지 전송 완료", data);

        return new RsData<>("200", "채팅 메시지 전송 완료", responseDto);
    }

    // 이미지 전송 API 등은 필요에 맞게 추가 구현 예정
}
