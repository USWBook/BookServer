package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private UUID roomId;
    private UUID senderId;
    private UUID receiverId;
    private ChatRoom chatRoom;
    private User senderUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roomId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .sender(senderId)
                .receiver(receiverId)
                .userCount(2)
                .build();

        senderUser = User.builder()
                .id(senderId)
                .build();
    }

    @Test
    @DisplayName("채팅 메시지 전송 성공")
    void sendChatMessage_success() {
        SendMessageRequestDto dto = new SendMessageRequestDto(
                roomId,
                senderId,
                UUID.randomUUID(),  // messageId - 실제 사용 안됨
                receiverId,
                "Hello world"
        );

        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
        given(userRepository.findById(senderId)).willReturn(Optional.of(senderUser));

        ChatMessage savedMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoomId(roomId)
                .sender(senderUser)
                .content("Hello world")
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        ChatMessage result = chatService.sendChatMessage(dto);

        assertThat(result).isNotNull();
        assertThat(result.getChatRoomId()).isEqualTo(roomId);
        assertThat(result.getSender().getId()).isEqualTo(senderId);
        assertThat(result.getContent()).isEqualTo("Hello world");

        then(chatMessageRepository).should().save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅 메시지 전송 실패 - 권한 없음")
    void sendChatMessage_unauthorizedSender_throwsException() {
        UUID otherUserId = UUID.randomUUID();
        SendMessageRequestDto dto = new SendMessageRequestDto(
                roomId,
                otherUserId,  // senderId 아닌 사람
                UUID.randomUUID(),
                receiverId,
                "Message"
        );

        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);

        assertThatThrownBy(() -> chatService.sendChatMessage(dto))
                .isInstanceOf(ChatAccessDeniedException.class);
    }

    @Test
    @DisplayName("채팅 메시지 전송 실패 - 빈 메시지")
    void sendChatMessage_blankMessage_throwsException() {
        SendMessageRequestDto dto = new SendMessageRequestDto(
                roomId,
                senderId,
                UUID.randomUUID(),
                receiverId,
                "   "  // 공백 메시지
        );

        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);

        assertThatThrownBy(() -> chatService.sendChatMessage(dto))
                .isInstanceOf(InvalidChatArgumentException.class);
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 성공")
    void getMessages_success() {
        ChatMessage msg1 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoomId(roomId)
                .content("msg1")
                .sender(senderUser)
                .sentAt(LocalDateTime.now())
                .build();
        ChatMessage msg2 = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoomId(roomId)
                .content("msg2")
                .sender(senderUser)
                .sentAt(LocalDateTime.now())
                .build();

        List<ChatMessage> messages = List.of(msg1, msg2);

        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
        given(chatMessageRepository.findByChatRoomId(roomId)).willReturn(messages);

        List<ChatMessage> result = chatService.getMessages(roomId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(messages);
    }

}
