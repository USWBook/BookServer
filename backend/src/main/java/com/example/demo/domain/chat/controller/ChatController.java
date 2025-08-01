package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.*;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.global.response.RsData;
import com.example.demo.global.response.Empty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.domain.chat.dto.request.LeaveChatRoomRequestDto;
import com.example.demo.domain.chat.dto.response.LeaveChatRoomResponseDto;
import com.example.demo.domain.chat.dto.response.DeleteChatRoomResponseDto;

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

    // 채팅방 생성 (POST)
    @PostMapping("/room")
    public RsData<CreateChatRoomResponseDto> createRoom(@RequestBody CreateChatRoomRequestDto dto) {
        ChatRoom room = chatRoomService.createOrGetRoom(dto);
        return new RsData<>("200", "채팅방 생성 완료", CreateChatRoomResponseDto.from(room));
    }


    /**
     * 채팅방 입장 API
     * GET /rooms/{roomId}/?userId={userId}
     *
     * @param roomId 채팅방 UUID (PathVariable)
     * @param userId 사용자 UUID (RequestParam, 필수)
     *
     * userId 쿼리 파라미터를 반드시 포함해야 하며,
     * 이 값이 없으면 400 Bad Request 에러가 발생합니다.
     */
    // 채팅방 입장 (GET)
    @GetMapping("/rooms/{roomId}")
    public RsData<EnterChatRoomResponseDto.Data> enterChatRoom(
            @PathVariable UUID roomId,
            @RequestParam UUID userId) {

        ChatRoom room;
        try {
            room = chatRoomService.findByRoomId(roomId);
        } catch (ChatRoomNotFoundException ex) {
            return new RsData<>("404", "채팅방을 찾을 수 없습니다.", null);
        }

        if (!room.getSender().equals(userId) && !room.getReceiver().equals(userId)) {
            return new RsData<>("403", "채팅방 입장 권한이 없습니다.", null);
        }

        EnterChatRoomResponseDto.Data data = new EnterChatRoomResponseDto.Data(
                room.getRoomId().toString(),
                room.getPostId() != null ? room.getPostId().toString() : null,
                room.getName(),
                room.getUserCount(),
                room.getLastMessage(),
                room.getLastTimestamp()
        );

        // RsData에 Data 객체만 담아서 반환
        return new RsData<>("200", "채팅방 입장 성공", data);
    }


    // 내 채팅방 목록 조회 (GET)
    @GetMapping("/rooms")
    public RsData<List<ListChatRoomsResponseDto.ChatRoomDto>> listRooms(@RequestParam UUID userId) {
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

        return new RsData<>("200", "나의 채팅방 목록 조회 성공", dtos);
    }


    // 채팅방 하나 조회 (GET)
    @GetMapping("/room/{roomId}")
    public RsData<ChatRoom> getRoom(@PathVariable UUID roomId) {
        return Optional.ofNullable(chatRoomService.findByRoomId(roomId))
                .map(room -> new RsData<>("200", "채팅방 조회 성공", room))
                .orElseGet(() -> new RsData<>("404", "채팅방을 찾을 수 없습니다."));
    }

    // 채팅방 메시지 수신 (GET)
    @GetMapping("/rooms/{roomId}/messages")
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

    // 채팅 메시지 송신 (POST)
    @PostMapping("/rooms/{roomId}/messages")
    public RsData<SendMessageResponseDto.Data> sendChatMessage(
            @PathVariable UUID roomId,
            @RequestBody SendMessageRequestDto request) {

        ChatMessage savedMessage = chatService.sendChatMessage(request);

        SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        return new RsData<>("200", "채팅 메시지 전송 완료", data);
    }

    //이미지 전송
    @PostMapping("/rooms/{roomId}/images")
    public RsData<SendImageResponseDto.Data> sendImageMessage(
            @PathVariable UUID roomId,
            @RequestParam UUID senderId,
            @RequestPart MultipartFile image) {

        ChatMessage imageMessage = chatService.sendImageMessage(roomId, senderId, image);

        SendImageResponseDto.Data data = new SendImageResponseDto.Data(
                imageMessage.getId(),
                imageMessage.getChatRoomId(),         // roomId 반환
                imageMessage.getSender().getId(),     // senderId 반환
                imageMessage.getImageUrl(),
                imageMessage.getSentAt()
        );
        return new RsData<>("200", "이미지 메시지 전송 완료", data);
    }

    //채팅방 나가기 (논리적 삭제)
    @PostMapping("/rooms/{roomId}/leave")
    public RsData<Empty> leaveChatRoom(
            @PathVariable UUID roomId,
            @RequestBody LeaveChatRoomRequestDto request) {
        chatRoomService.leaveChatRoom(roomId, request.userId());
        return new RsData<>("200", "채팅방 나가기 완료", null); // 또는 new Empty()
    }

    //채팅방 삭제
    @DeleteMapping("/rooms/{roomId}")
    public RsData<DeleteChatRoomResponseDto> deleteChatRoom(
            @PathVariable UUID roomId,
            @RequestParam String username) {
        chatRoomService.deleteChatRoom(roomId, username);
        return new RsData<>("200", "채팅방 삭제 완료", new DeleteChatRoomResponseDto(200, "채팅방 삭제 완료"));
    }


}
