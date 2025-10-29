package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.SendImageRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    //메시지 전송
    @Transactional
    public ChatMessage sendChatMessage(SendMessageRequestDto dto, UUID senderId) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(dto.roomId());

        // 채팅방 참가자인지 검사
        if (!chatRoom.getSender().equals(senderId) && !chatRoom.getReceiver().equals(senderId)) {
            throw new ChatAccessDeniedException();
        }

        if (dto.message() == null || dto.message().isBlank()) {
            throw new InvalidChatArgumentException();
        }

        User senderUser = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(dto.roomId())
                .sender(senderUser)
                .content(dto.message())
                .isRead(false)
                .readCount(2) // 처음에 두명 모두 안 읽은 상태
                .build();

        return chatMessageRepository.save(message);
    }


    public List<ChatMessage> getMessages(UUID roomId) {
        chatRoomService.findByRoomId(roomId);  // 방 존재 확인용
        return chatMessageRepository.findByChatRoomId(roomId);
    }

    // 이미지 전송
    @Transactional
    public ChatMessage sendImageMessage(SendImageRequestDto dto, UUID senderId) {
        ChatRoom chatRoom = chatRoomService.findByRoomId(dto.roomId());

        // 채팅방 참가자인지 검사
        if (!chatRoom.getSender().equals(senderId) && !chatRoom.getReceiver().equals(senderId)) {
            throw new ChatAccessDeniedException();
        }

        MultipartFile imageFile = dto.image();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new InvalidChatArgumentException();
        }

        User senderUser = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자 없음"));

        // 파일 저장 처리
        String uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "chat-images").toString();
        String filename = UUID.randomUUID() + "_" + sanitizeFilename(imageFile.getOriginalFilename());
        Path filepath = Paths.get(uploadDir, filename);
        try {
            Files.createDirectories(filepath.getParent());
            imageFile.transferTo(filepath.toFile());
        } catch (Exception e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

        String imageUrl = "/chat-images/" + filename;

        // 메시지 entity 저장
        ChatMessage imageMessage = ChatMessage.builder()
                .chatRoomId(dto.roomId())
                .sender(senderUser)
                .imageUrl(imageUrl)
                .isRead(false)
                .build();

        return chatMessageRepository.save(imageMessage);
    }

    // 파일명에서 한글 및 특수문자 등 안전하지 않은 문자 _ 로 변환
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void markMessagesRead(UUID roomId, LocalDateTime lastReadAt, UUID readerId) {
        List<ChatMessage> messages =
                chatMessageRepository.findByChatRoomIdAndSentAtLessThanEqual(roomId, lastReadAt);

        for (ChatMessage m : messages) {
            // 내가 보낸 메시지는 제외
            if (!m.getSender().getId().equals(readerId) && m.getReadCount() > 0) {
                m.decreaseReadCount();
            }
        }
        chatMessageRepository.saveAll(messages);
    }
}
