package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage sendChatMessage(SendMessageRequestDto dto) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(dto.roomId());

        // 채팅방 참가자인지 검사
        if (!chatRoom.getSender().equals(dto.senderId()) && !chatRoom.getReceiver().equals(dto.senderId())) {
            throw new ChatAccessDeniedException();
        }

        if (dto.message() == null || dto.message().isBlank()) {
            throw new InvalidChatArgumentException();
        }

        User senderUser = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.roomId())
                .sender(senderUser)
                .content(dto.message())
                .isRead(false)
                .build();

        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessages(UUID roomId) {
        chatRoomService.findByRoomId(roomId);  // 방 존재 확인용
        return chatMessageRepository.findByChatRoomId(roomId);
    }

    //메시지 전송
    @Transactional
    public ChatMessage sendImageMessage(UUID roomId, UUID senderId, MultipartFile imageFile) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);

        // 채팅방 참가자인지 검사
        if (!chatRoom.getSender().equals(senderId) && !chatRoom.getReceiver().equals(senderId)) {
            throw new ChatAccessDeniedException();
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new InvalidChatArgumentException();
        }
        User senderUser = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자 없음"));

        // 1. 파일 저장 (예시: 로컬 서버 경로)
        String uploadDir = "C:/url";
        String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path filepath = Paths.get(uploadDir, filename);
        try {
            Files.createDirectories(filepath.getParent());
            imageFile.transferTo(filepath.toFile());
        } catch (Exception e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
        String imageUrl = "/static/chat-images/" + filename; // 프론트에서 접근 가능한 엔드포인트로 가정

        // 2. 메시지 저장
        ChatMessage imageMessage = ChatMessage.builder()
                .chatRoomId(roomId)
                .sender(senderUser)
                .imageUrl(imageUrl)
                .isRead(false)
                .build();

        return chatMessageRepository.save(imageMessage);
    }
}
