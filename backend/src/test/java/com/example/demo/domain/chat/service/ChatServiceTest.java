//package com.example.demo.domain.chat.service;
//
//import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
//import com.example.demo.domain.chat.entity.ChatMessage;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
//import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
//import com.example.demo.domain.chat.repository.ChatMessageRepository;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ChatServiceTest {
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository;
//
//    @Mock
//    private ChatRoomService chatRoomService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private ChatService chatService;
//
//    private UUID roomId;
//    private UUID senderId;
//    private UUID receiverId;
//    private ChatRoom chatRoom;
//    private User senderUser;
//
//    @BeforeEach
//    void setUp() {
//        roomId = UUID.randomUUID();
//        senderId = UUID.randomUUID();
//        receiverId = UUID.randomUUID();
//
//        senderUser = User.builder().id(senderId).build();
//        chatRoom = new ChatRoom();
//        chatRoom.setRoomId(roomId);
//        chatRoom.setSender(senderId);
//        chatRoom.setReceiver(receiverId);
//    }
//
//    // ========================================
//    // 채팅 메시지 전송 검증 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("채팅 메시지 전송 성공")
//    void sendChatMessage_success() {
//        SendMessageRequestDto dto = new SendMessageRequestDto(roomId, senderId, "hello");
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//        given(userRepository.findById(senderId)).willReturn(Optional.of(senderUser));
//
//        ChatMessage expectedMessage = ChatMessage.builder()
//                .chatRoomId(roomId)
//                .sender(senderUser)
//                .content("hello")
//                .isRead(false)
//                .build();
//
//        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(expectedMessage);
//
//        ChatMessage actual = chatService.sendChatMessage(dto);
//
//        assertThat(actual.getChatRoomId()).isEqualTo(roomId);
//        assertThat(actual.getSender()).isEqualTo(senderUser);
//        assertThat(actual.getContent()).isEqualTo("hello");
//        then(chatMessageRepository).should().save(any(ChatMessage.class));
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 전송 실패 - 채팅방 참가자가 아니면 예외 발생")
//    void sendChatMessage_userNotParticipant_throwsException() {
//        SendMessageRequestDto dto = new SendMessageRequestDto(roomId, UUID.randomUUID(), "hello");
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//
//        assertThatThrownBy(() -> chatService.sendChatMessage(dto))
//                .isInstanceOf(ChatAccessDeniedException.class);
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 전송 실패 - 메시지가 null 또는 공백이면 예외 발생")
//    void sendChatMessage_invalidMessage_throwsException() {
//        SendMessageRequestDto dtoNull = new SendMessageRequestDto(roomId, senderId, null);
//        SendMessageRequestDto dtoBlank = new SendMessageRequestDto(roomId, senderId, "    ");
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//
//        assertThatThrownBy(() -> chatService.sendChatMessage(dtoNull))
//                .isInstanceOf(InvalidChatArgumentException.class);
//
//        assertThatThrownBy(() -> chatService.sendChatMessage(dtoBlank))
//                .isInstanceOf(InvalidChatArgumentException.class);
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 전송 실패 - 발신자 사용자 없으면 예외 발생")
//    void sendChatMessage_senderNotFound_throwsException() {
//        SendMessageRequestDto dto = new SendMessageRequestDto(roomId, senderId, "msg");
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//        given(userRepository.findById(senderId)).willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> chatService.sendChatMessage(dto))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("보낸 사용자 없음");
//    }
//
//    // ========================================
//    // 채팅 메시지 수신 / 조회 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("채팅 메시지 목록 조회 성공")
//    void getMessages_success() {
//        List<ChatMessage> messages = List.of(
//                ChatMessage.builder().content("msg1").build(),
//                ChatMessage.builder().content("msg2").build()
//        );
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//        given(chatMessageRepository.findByChatRoomId(roomId)).willReturn(messages);
//
//        List<ChatMessage> actual = chatService.getMessages(roomId);
//
//        assertThat(actual).hasSize(2);
//        then(chatRoomService).should().findByRoomId(roomId);
//        then(chatMessageRepository).should().findByChatRoomId(roomId);
//    }
//
//    // ========================================
//    // 이미지 메시지 전송 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("이미지 메시지 전송 성공")
//    void sendImageMessage_success() throws Exception {
//        MultipartFile imageFile = mock(MultipartFile.class);
//        User user = User.builder().id(senderId).build();
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//        given(userRepository.findById(senderId)).willReturn(Optional.of(user));
//        given(imageFile.isEmpty()).willReturn(false);
//        given(imageFile.getOriginalFilename()).willReturn("image.png");
//
//        // transferTo(File) 명확하게 모킹
//        doAnswer(invocation -> null).when(imageFile).transferTo(any(File.class));
//
//        given(chatMessageRepository.save(any(ChatMessage.class))).willAnswer(invocation -> invocation.getArgument(0));
//
//        ChatMessage result = chatService.sendImageMessage(roomId, senderId, imageFile);
//
//        assertThat(result.getChatRoomId()).isEqualTo(roomId);
//        assertThat(result.getSender()).isEqualTo(user);
//        assertThat(result.getImageUrl()).contains("image.png");
//        then(imageFile).should().transferTo(any(File.class));
//        then(chatMessageRepository).should().save(any(ChatMessage.class));
//    }
//
//    @Test
//    @DisplayName("이미지 메시지 전송 실패 - 채팅방 참가자가 아니면 예외")
//    void sendImageMessage_notParticipant_throwsException() {
//        MultipartFile imageFile = mock(MultipartFile.class);
//        UUID invalidSenderId = UUID.randomUUID();
//
//        ChatRoom room = new ChatRoom();
//        room.setRoomId(roomId);
//        room.setSender(senderId);
//        room.setReceiver(receiverId);
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(room);
//
//        assertThatThrownBy(() -> chatService.sendImageMessage(roomId, invalidSenderId, imageFile))
//                .isInstanceOf(ChatAccessDeniedException.class);
//    }
//
//    @Test
//    @DisplayName("이미지 메시지 전송 실패 - 파일 없으면 예외 발생")
//    void sendImageMessage_noFile_throwsException() {
//        MultipartFile imageFile = mock(MultipartFile.class);
//
//        ChatRoom room = new ChatRoom();
//        room.setRoomId(roomId);
//        room.setSender(senderId);
//        room.setReceiver(receiverId);
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(room);
//        given(imageFile.isEmpty()).willReturn(true);
//
//        assertThatThrownBy(() -> chatService.sendImageMessage(roomId, senderId, imageFile))
//                .isInstanceOf(InvalidChatArgumentException.class);
//    }
//
//    @Test
//    @DisplayName("이미지 메시지 전송 실패 - 사용자 조회 실패 시 예외 발생")
//    void sendImageMessage_senderNotFound_throwsException() {
//        MultipartFile imageFile = mock(MultipartFile.class);
//
//        ChatRoom room = new ChatRoom();
//        room.setRoomId(roomId);
//        room.setSender(senderId);
//        room.setReceiver(receiverId);
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(room);
//        given(userRepository.findById(senderId)).willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> chatService.sendImageMessage(roomId, senderId, imageFile))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("보낸 사용자 없음");
//    }
//
//    @Test
//    @DisplayName("이미지 메시지 전송 실패 - 이미지 저장 실패 시 예외 발생")
//    void sendImageMessage_fileSaveFails_throwsException() throws Exception {
//        MultipartFile imageFile = mock(MultipartFile.class);
//
//        ChatRoom room = new ChatRoom();
//        room.setRoomId(roomId);
//        room.setSender(senderId);
//        room.setReceiver(receiverId);
//
//        given(chatRoomService.findByRoomId(roomId)).willReturn(room);
//        given(imageFile.isEmpty()).willReturn(false);
//        given(imageFile.getOriginalFilename()).willReturn("img.jpg");
//
//        willThrow(new RuntimeException("disk full"))
//                .given(imageFile).transferTo(any(File.class));
//
//        given(userRepository.findById(senderId)).willReturn(Optional.of(senderUser));
//
//        assertThatThrownBy(() -> chatService.sendImageMessage(roomId, senderId, imageFile))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("이미지 저장 실패");
//    }
//}
