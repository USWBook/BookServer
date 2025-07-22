package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatMessage.MessageType;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.ChatImageUploadException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final String CHAT_ROOMS = "CHAT_ROOM";

    private final ChannelTopic channelTopic;

    @Qualifier("chatRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChatRoomService chatRoomService;
    private final ChatMessageRepository chatMessageRepository;

    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

    @PostConstruct
    public void init() {
        this.hashOpsChatRoom = redisTemplate.opsForHash();
    }

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
        ChatRoom chatRoom = chatRoomService.findByRoomId(chatMessage.getRoomId());

        if (!chatRoom.getSender().equals(chatMessage.getSender()) &&
                !chatRoom.getReceiver().equals(chatMessage.getSender())) {
            throw new ChatAccessDeniedException();
        }

        if (chatMessage.getType() == MessageType.TALK &&
                (chatMessage.getMessage() == null || chatMessage.getMessage().isBlank())) {
            throw new InvalidChatArgumentException();
        }

        UUID receiver = chatRoomService.findOther(chatRoom.getRoomId(), chatMessage.getSender());

        if (chatMessage.getDeleteStatus(receiver)) {
            chatMessage.setDeleteStatus(receiver, false);
            hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);
        }

        ChatMessage messageToSend = ChatMessage.builder()
                .type(chatMessage.getType())
                .roomId(chatRoom.getRoomId())
                .sender(chatMessage.getSender())
                .receiver(receiver)
                .message(chatMessage.getType() == MessageType.TALK ? chatMessage.getMessage() : "")
                .image(chatMessage.getImage())
                .userCount(chatRoomService.getUserCount(chatMessage.getRoomId()))
                .build();

        if (chatMessage.getType() == MessageType.TALK) {
            chatRoom.setLastMessage(chatMessage.getMessage());
            chatRoom.setLastTimestamp(messageToSend.getTimestamp());
        }

        redisTemplate.convertAndSend(channelTopic.getTopic(), messageToSend);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId().toString(), chatRoom);
        chatMessageRepository.save(messageToSend);
    }

    /**
     * 메시지 목록 조회
     */
    public List<ChatMessage> getMessages(UUID roomId) {
        chatRoomService.findByRoomId(roomId);
        return chatMessageRepository.findByRoomId(roomId);
    }

    /**
     * 이미지 전송
     */
    @Transactional
    public ChatMessage sendImage(UUID roomId, UUID sender, UUID receiver, MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new InvalidChatArgumentException();
        }

        ChatRoom chatRoom = chatRoomService.findByRoomId(roomId);

        if (!chatRoom.getSender().equals(sender) && !chatRoom.getReceiver().equals(sender)) {
            throw new ChatAccessDeniedException();
        }

        String imageUrl;
        try {
            imageUrl = "https://your.s3.bucket/" + imageFile.getOriginalFilename(); // 예시용
        } catch (Exception e) {
            throw new ChatImageUploadException();
        }

        ChatMessage imageMessage = ChatMessage.builder()
                .type(MessageType.IMAGE)
                .roomId(roomId)
                .sender(sender)
                .receiver(receiver)
                .message("")
                .image(imageUrl)
                .userCount(chatRoomService.getUserCount(roomId))
                .build();

        redisTemplate.convertAndSend(channelTopic.getTopic(), imageMessage);
        chatMessageRepository.save(imageMessage);

        return imageMessage;
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesRead(UUID roomId, String username) {
        System.out.println("사용자 " + username + "가 " + roomId + " 메시지를 읽음");
    }
}
