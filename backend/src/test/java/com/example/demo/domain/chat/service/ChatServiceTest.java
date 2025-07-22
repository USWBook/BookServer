package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatMessage.MessageType;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class ChatServiceTest {

    @Mock
    private ChannelTopic channelTopic;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOpsChatRoom;

    @InjectMocks
    private ChatService chatService;

    private UUID roomId;
    private UUID senderId;
    private UUID receiverId;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // opsForHash 반환값 지정
        given(redisTemplate.opsForHash()).willReturn(hashOpsChatRoom);
        // 필드 직접 주입
        ReflectionTestUtils.setField(chatService, "hashOpsChatRoom", hashOpsChatRoom);

        roomId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .sender(senderId)
                .receiver(receiverId)
                .userCount(2)
                .isDelete(new HashMap<>())
                .build();
    }

    @Test
    @DisplayName("채팅 메시지 전송 성공")
    void sendChatMessage_success() {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(MessageType.TALK)
                .roomId(roomId)
                .sender(senderId)
                .message("hello")
                .build();

        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
        given(chatRoomService.findOther(roomId, senderId)).willReturn(receiverId);
        given(chatRoomService.getUserCount(roomId)).willReturn(2);

        chatService.sendChatMessage(chatMessage);

        verify(redisTemplate).convertAndSend(any(), any(ChatMessage.class));
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅 메시지 전송 실패 - 권한 없음")
    void sendChatMessage_unauthorizedSender_throwsException() {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(MessageType.TALK)
                .roomId(roomId)
                .sender(UUID.randomUUID()) // senderId와 다름
                .message("msg")
                .build();
        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);

        assertThatThrownBy(() -> chatService.sendChatMessage(chatMessage))
                .isInstanceOf(com.example.demo.domain.chat.exception.ChatAccessDeniedException.class);
    }

    @Test
    @DisplayName("채팅 메시지 전송 실패 - 빈 메시지")
    void sendChatMessage_blankMessage_throwsException() {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(MessageType.TALK)
                .roomId(roomId)
                .sender(senderId)
                .message("")
                .build();
        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);

        assertThatThrownBy(() -> chatService.sendChatMessage(chatMessage))
                .isInstanceOf(com.example.demo.domain.chat.exception.InvalidChatArgumentException.class);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 성공")
    void getMessages_success() {
        List<ChatMessage> messages = List.of(
                ChatMessage.builder().roomId(roomId).message("1").build(),
                ChatMessage.builder().roomId(roomId).message("2").build()
        );
        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
        given(chatMessageRepository.findByRoomId(roomId)).willReturn(messages);

        List<ChatMessage> result = chatService.getMessages(roomId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공")
    void sendImage_success() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("image.jpg");
        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
        given(chatRoomService.getUserCount(roomId)).willReturn(2);

        ChatMessage imageMessage = chatService.sendImage(roomId, senderId, receiverId, file);

        assertThat(imageMessage.getType()).isEqualTo(MessageType.IMAGE);
        assertThat(imageMessage.getImage()).contains("image.jpg");
        verify(redisTemplate).convertAndSend(any(), any(ChatMessage.class));
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("이미지 메시지 전송 실패 - 파일이 null")
    void sendImage_fileNull_throwsException() {
        assertThatThrownBy(() -> chatService.sendImage(roomId, senderId, receiverId, null))
                .isInstanceOf(com.example.demo.domain.chat.exception.InvalidChatArgumentException.class);
    }
}
