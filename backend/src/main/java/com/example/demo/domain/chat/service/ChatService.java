package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatMessage.MessageType;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.ChatImageUploadException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic; // Redis pub/sub 토픽
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;

    private static final String CHAT_ROOMS = "CHAT_ROOM";

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom; // Redis에서 채팅방 관리

    /**
     * destination에서 roomId 추출
     */
    public UUID getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1) {
            return UUID.fromString(destination.substring(lastIndex + 1));
        } else {
            return null;
        }
    }

    /**
     * 채팅 메시지 전송
     */
    @Transactional
    public void sendChatMessage(ChatMessage chatMessage) {
        // 🔥 채팅방 존재 확인 — 없으면 예외 발생
        ChatRoom chatRoom = chatRoomService.findByRoomId(chatMessage.getRoomId());

        // 🔥 참여자 권한 확인 — sender가 sender/receiver 중 하나인지 확인
        if (!chatRoom.getSender().equals(chatMessage.getSender()) &&
                !chatRoom.getReceiver().equals(chatMessage.getSender())) {
            throw new ChatAccessDeniedException();
        }

        // 🔥 채팅 메시지 내용 유효성 검사
        if (chatMessage.getType() == MessageType.TALK &&
                (chatMessage.getMessage() == null || chatMessage.getMessage().isBlank())) {
            throw new InvalidChatArgumentException();
        }

        UUID receiver = chatRoomService.findOther(chatRoom.getRoomId(), chatMessage.getSender());

        // 수신자가 채팅방을 삭제한 상태라면 복구 (논리적 삭제 해제)
        if (chatMessage.getDeleteStatus(receiver)) {
            chatMessage.setDeleteStatus(receiver, false);
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);
        }

        // 새 메시지 생성
        ChatMessage messageToSend = ChatMessage.builder()
                .type(chatMessage.getType())
                .roomId(chatRoom.getRoomId())
                .sender(chatMessage.getSender())
                .receiver(receiver)
                .message(chatMessage.getType() == MessageType.TALK ? chatMessage.getMessage() : "")
                .image(chatMessage.getImage())
                .userCount(chatRoomService.getUserCount(chatMessage.getRoomId()))
                .build();

        // 마지막 메시지, 타임스탬프 저장
        if (chatMessage.getType() == MessageType.TALK) {
            chatRoom.setLastMessage(chatMessage.getMessage());
            chatRoom.setLastTimestamp(messageToSend.getTimestamp());
        }

        // Redis Pub/Sub 발행
        redisTemplate.convertAndSend(channelTopic.getTopic(), messageToSend);

        // Redis 채팅방 갱신
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);

        // DB 메시지 저장
        chatMessageRepository.save(messageToSend);
    }

    /**
     * 메시지 목록 조회 (1:1)
     */
    public List<ChatMessage> getMessages(UUID roomId) {
        // 🔥 채팅방 존재 확인 안 하면 NPE 가능 — 예외 처리
        chatRoomService.findByRoomId(roomId);
        return chatMessageRepository.findByRoomId(roomId);
    }

    /**
     * 이미지 전송
     */
    @Transactional
    public ChatMessage sendImage(UUID roomId, UUID sender, UUID receiver, MultipartFile imageFile) {

        // 🔥 파일이 없는 경우
        if (imageFile == null || imageFile.isEmpty()) {
            throw new InvalidChatArgumentException();
        }

        // 🔥 채팅방 확인
        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);

        // 🔥 참여자 권한 확인
        if (!chatRoom.getSender().equals(sender) && !chatRoom.getReceiver().equals(sender)) {
            throw new ChatAccessDeniedException();
        }

        // 🔥 파일 업로드 처리 (S3 등)
        String imageUrl;
        try {
            imageUrl = "https://your.s3.bucket/" + imageFile.getOriginalFilename(); // 예시
        } catch (Exception e) {
            throw new ChatImageUploadException();
        }

        // 메시지 생성
        ChatMessage imageMessage = ChatMessage.builder()
                .type(MessageType.IMAGE)
                .roomId(roomId)
                .sender(sender)
                .receiver(receiver)
                .message("") // 이미지 메시지는 텍스트 없음
                .image(imageUrl)
                .userCount(chatRoomService.getUserCount(roomId))
                .build();

        // Redis 발행 & DB 저장
        redisTemplate.convertAndSend(channelTopic.getTopic(), imageMessage);
        chatMessageRepository.save(imageMessage);

        return imageMessage;
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesRead(UUID roomId, String username) {
        // 추후 확장용
        System.out.println("사용자 " + username + "가 " + roomId + " 메시지를 읽음");
    }
}
