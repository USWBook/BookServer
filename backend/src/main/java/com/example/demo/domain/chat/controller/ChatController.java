package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.dto.response.*;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.response.RsData;
import com.example.demo.global.response.Empty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.domain.chat.dto.request.LeaveChatRoomRequestDto;
import com.example.demo.domain.chat.dto.response.LeaveChatRoomResponseDto;
import com.example.demo.domain.chat.dto.response.DeleteChatRoomResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatRoomService chatRoomService;
    private final ChatService chatService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);


    //채팅방 생성
    @PostMapping("/room")
    public RsData<CreateChatRoomResponseDto> requestChatRoom(@RequestBody CreateChatRoomRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            log.error("인증 정보가 없습니다. authentication=" + authentication);
            throw new RuntimeException("인증 정보가 없습니다.");
        }
        String email = authentication.getName();
        log.info("요청한 사용자 email={}", email);

        // 1) 이메일로 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User 없는 email={}", email);
                    return new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다. email=" + email);
                });

        log.info("조회된 userId={}", user.getId());

        // 2) 이제 postId로 채팅방 생성 시도
        try {
            ChatRoom room = chatRoomService.createOrGetRoom(dto, user.getId());
            return new RsData<>("200", "채팅 요청 완료", CreateChatRoomResponseDto.from(room));
        } catch(Exception ex) {
            log.error("채팅방 생성 중 오류. postId={}, buyerId={}", dto.postId(), user.getId(), ex);
            throw ex; // 필요하면 커스텀 에러로 변환
        }
    }

    // 채팅방 입장(권한 체크)
    @GetMapping("/room/{roomId}")
    public RsData<EnterChatRoomResponseDto.Data> enterChatRoom(
            @PathVariable UUID roomId,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 사용자를 찾을 수 없습니다."));

        UUID userId = user.getId();

        ChatRoom room = chatRoomService.findByRoomId(roomId);

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
        return new RsData<>("200", "채팅방 입장 성공", data);
    }

    // 내 채팅방 목록 조회
    @GetMapping("/rooms")
    public RsData<List<ListChatRoomsResponseDto.ChatRoomDto>> listRooms(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }
        UUID userId = UUID.fromString(authentication.getName());
        List<ChatRoom> rooms = chatRoomService.findRoomByUser(userId);
        List<ListChatRoomsResponseDto.ChatRoomDto> dtos = rooms.stream()
                .map(room -> new ListChatRoomsResponseDto.ChatRoomDto(
                        room.getRoomId().toString(),
                        room.getPostId() != null ? room.getPostId().toString() : null,
                        room.getName(),
                        room.getUserCount(),
                        room.getLastMessage(),
                        room.getLastTimestamp()
                )).collect(Collectors.toList());

        return new RsData<>("200", "나의 채팅방 목록 조회 성공", dtos);
    }

    // 채팅방 메시지 수신
    @GetMapping("/rooms/{roomId}/messages")
    public RsData<List<SendMessageResponseDto.Data>> getMessages(@PathVariable UUID roomId, Authentication authentication) {
        ChatRoom room = chatRoomService.findByRoomId(roomId);
        UUID userId = UUID.fromString(authentication.getName());
        if (!room.getSender().equals(userId) && !room.getReceiver().equals(userId)) {
            return new RsData<>("403", "채팅방 조회 권한 없음", null);
        }
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
            @RequestBody SendMessageRequestDto request,
            Authentication authentication) {
        UUID senderId = UUID.fromString(authentication.getName());
        // DTO에 senderId 넣어서 호출!
        SendMessageRequestDto fixedRequest = new SendMessageRequestDto(
                roomId,
                senderId,
                request.message()
        );
        ChatMessage savedMessage = chatService.sendChatMessage(fixedRequest);

        SendMessageResponseDto.Data data = new SendMessageResponseDto.Data(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
        );

        return new RsData<>("200", "채팅 메시지 전송 완료", data);
    }

    // 이미지 전송
    @PostMapping("/rooms/{roomId}/images")
    public RsData<SendImageResponseDto.Data> sendImageMessage(
            @PathVariable UUID roomId,
            @RequestPart MultipartFile image,
            Authentication authentication) {
        UUID senderId = UUID.fromString(authentication.getName());
        ChatMessage imageMessage = chatService.sendImageMessage(roomId, senderId, image);
        SendImageResponseDto.Data data = new SendImageResponseDto.Data(
                imageMessage.getId(),
                imageMessage.getChatRoomId(),
                imageMessage.getSender().getId(),
                imageMessage.getImageUrl(),
                imageMessage.getSentAt()
        );
        return new RsData<>("200", "이미지 메시지 전송 완료", data);
    }

    // 채팅방 나가기 (논리 삭제)
    @PostMapping("/rooms/{roomId}/leave")
    public RsData<Empty> leaveChatRoom(
            @PathVariable UUID roomId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        chatRoomService.leaveChatRoom(roomId, userId);
        return new RsData<>("200", "채팅방 나가기 완료", null);
    }

    // 채팅방 삭제
//    @DeleteMapping("/rooms/{roomId}")
//    public RsData<DeleteChatRoomResponseDto> deleteChatRoom(
//            @PathVariable UUID roomId,
//            Authentication authentication) {
//        UUID userId = UUID.fromString(authentication.getName());
//        chatRoomService.deleteChatRoom(roomId, userId.toString());
//        return new RsData<>("200", "채팅방 삭제 완료", new DeleteChatRoomResponseDto(200, "채팅방 삭제 완료"));
//    }
}

