//package com.example.demo.domain.chat.service;
//
//import com.example.demo.domain.chat.dto.request.SendImageRequestDto;
//import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
//import com.example.demo.domain.chat.entity.ChatMessage;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
//import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
//import com.example.demo.domain.chat.repository.ChatMessageRepository;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class ChatServiceTest {
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
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @DisplayName("✅ 메시지 전송 성공")
//    void sendChatMessage_success() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//        SendMessageRequestDto dto = mock(SendMessageRequestDto.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.message()).thenReturn("Hello");
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(senderId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        User senderUser = mock(User.class);
//        when(userRepository.findById(senderId)).thenReturn(Optional.of(senderUser));
//
//        ChatMessage savedMessage = mock(ChatMessage.class);
//        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
//
//        ChatMessage result = chatService.sendChatMessage(dto, senderId);
//
//        assertEquals(savedMessage, result);
//        verify(chatMessageRepository).save(any(ChatMessage.class));
//    }
//
//    @Test
//    @DisplayName("⭕️ 메시지 전송 실패 - 채팅방 참가자 아님")
//    void sendChatMessage_notParticipant_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//        SendMessageRequestDto dto = mock(SendMessageRequestDto.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(UUID.randomUUID());
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        assertThrows(ChatAccessDeniedException.class, () -> chatService.sendChatMessage(dto, senderId));
//    }
//
//    @Test
//    @DisplayName("⭕️ 메시지 전송 실패 - 메시지 공백")
//    void sendChatMessage_blankMessage_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//        SendMessageRequestDto dto = mock(SendMessageRequestDto.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.message()).thenReturn("  "); // 공백
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(senderId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        assertThrows(InvalidChatArgumentException.class, () -> chatService.sendChatMessage(dto, senderId));
//    }
//
//    @Test
//    @DisplayName("⭕️ 메시지 전송 실패 - 사용자 없음")
//    void sendChatMessage_userNotFound_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//        SendMessageRequestDto dto = mock(SendMessageRequestDto.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.message()).thenReturn("Hello");
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(senderId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        when(userRepository.findById(senderId)).thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, () -> chatService.sendChatMessage(dto, senderId));
//    }
//
//    @Test
//    @DisplayName("✅ 채팅 메시지 목록 조회 성공")
//    void getMessages_success() {
//        UUID roomId = UUID.randomUUID();
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//
//        List<ChatMessage> messages = List.of(mock(ChatMessage.class), mock(ChatMessage.class));
//        when(chatMessageRepository.findByChatRoomId(roomId)).thenReturn(messages);
//
//        List<ChatMessage> result = chatService.getMessages(roomId);
//
//        assertEquals(messages, result);
//    }
//
//    @Test
//    @DisplayName("✅ 이미지 전송 성공")
//    void sendImageMessage_success() throws Exception {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//
//        SendImageRequestDto dto = mock(SendImageRequestDto.class);
//        MultipartFile imageFile = mock(MultipartFile.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.image()).thenReturn(imageFile);
//        when(imageFile.isEmpty()).thenReturn(false);
//        when(imageFile.getOriginalFilename()).thenReturn("image.png");
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(senderId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        User senderUser = mock(User.class);
//        when(userRepository.findById(senderId)).thenReturn(Optional.of(senderUser));
//
//        ChatMessage savedMessage = mock(ChatMessage.class);
//        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
//
//        // 파일 생성, 쓰기 부분은 실제로 테스트하지 않고 무시
//        doNothing().when(imageFile).transferTo((File) any());
//
//        ChatMessage result = chatService.sendImageMessage(dto, senderId);
//
//        assertEquals(savedMessage, result);
//        verify(chatMessageRepository).save(any(ChatMessage.class));
//    }
//
//    @Test
//    @DisplayName("⭕️ 이미지 전송 실패 - 이미지 없음")
//    void sendImageMessage_noImage_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//
//        SendImageRequestDto dto = mock(SendImageRequestDto.class);
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.image()).thenReturn(null);
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(senderId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        assertThrows(InvalidChatArgumentException.class, () -> chatService.sendImageMessage(dto, senderId));
//    }
//
//
//    @Test
//    @DisplayName("⭕️ 이미지 전송 실패 - 채팅방 참가자 아님")
//    void sendImageMessage_notParticipant_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//
//        SendImageRequestDto dto = mock(SendImageRequestDto.class);
//        MultipartFile imageFile = mock(MultipartFile.class);
//
//        when(dto.roomId()).thenReturn(roomId);
//        when(dto.image()).thenReturn(imageFile);
//        when(imageFile.isEmpty()).thenReturn(false);
//        when(imageFile.getOriginalFilename()).thenReturn("image.png");
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(UUID.randomUUID());
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        assertThrows(ChatAccessDeniedException.class, () -> chatService.sendImageMessage(dto, senderId));
//    }
//
//    @Test
//    @DisplayName("✅ getUserByEmail 정상 조회")
//    void getUserByEmail_success() {
//        String email = "test@example.com";
//        User user = mock(User.class);
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        User result = chatService.getUserByEmail(email);
//
//        assertEquals(user, result);
//    }
//
//    @Test
//    @DisplayName("⭕️ getUserByEmail 실패 - 사용자 없음")
//    void getUserByEmail_notFound_throws() {
//        String email = "noone@example.com";
//
//        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
//                () -> chatService.getUserByEmail(email));
//    }
//}
