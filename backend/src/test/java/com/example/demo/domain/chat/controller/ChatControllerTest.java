//package com.example.demo.domain.chat.controller;
//
//import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
//import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
//import com.example.demo.domain.chat.entity.ChatMessage;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.service.ChatRoomService;
//import com.example.demo.domain.chat.service.ChatService;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//class ChatControllerTest {
//
//    @InjectMocks
//    private ChatController chatController;
//
//    @Mock
//    private ChatRoomService chatRoomService;
//
//    @Mock
//    private ChatService chatService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private Authentication authentication;
//
//    private MockMvc mockMvc;
//    private ObjectMapper objectMapper;
//
//    private UUID roomId;
//    private UUID senderId;
//    private UUID receiverId;
//    private ChatRoom chatRoom;
//    private User user;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
//        objectMapper = new ObjectMapper();
//
//        roomId = UUID.randomUUID();
//        senderId = UUID.randomUUID();
//        receiverId = UUID.randomUUID();
//
//        user = User.builder()
//                .id(senderId)
//                .email("ignored@example.com") // 이메일 무시
//                .build();
//
//        chatRoom = ChatRoom.builder()
//                .roomId(roomId)
//                .sender(senderId)
//                .receiver(receiverId)
//                .userCount(2)
//                .lastMessage("hello")
//                .lastTimestamp(LocalDateTime.now().toString())
//                .build();
//
//        // 인증 무시: authentication.getName()이 UUID 문자열 반환하도록 모킹
//        given(authentication.getName()).willReturn(senderId.toString());
//
//
//        // 컨트롤러 내부 userRepository.findByEmail() 예외 방지용 모킹(Optional.of(user))
//        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
//    }
//
//    @Test
//    @DisplayName("채팅방 생성 & 입장")
//    void testEnterChatRoom_success() throws Exception {
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//
//        mockMvc.perform(get("/api/chat/room/" + roomId)
//                        .principal(authentication))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("채팅방 입장 성공"))
//                .andExpect(jsonPath("$.data.roomId").value(roomId.toString()));
//    }
//
//    @Test
//    @DisplayName("내 채팅방 목록 조회")
//    void testListRooms() throws Exception {
//        List<ChatRoom> rooms = List.of(chatRoom);
//
//        given(chatRoomService.findRoomByUser(any(UUID.class))).willReturn(rooms);
//
//        mockMvc.perform(get("/api/chat/rooms")
//                        .principal(authentication))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("나의 채팅방 목록 조회 성공"))
//                .andExpect(jsonPath("$.data[0].roomId").value(roomId.toString()));
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 수신")
//    void testGetMessages_success() throws Exception {
//        given(chatRoomService.findByRoomId(roomId)).willReturn(chatRoom);
//
//        ChatMessage chatMessage = ChatMessage.builder()
//                .id(UUID.randomUUID())
//                .chatRoomId(roomId)
//                .sender(user)
//                .content("test message")
//                .sentAt(LocalDateTime.now())
//                .build();
//
//        List<ChatMessage> messages = List.of(chatMessage);
//        given(chatService.getMessages(roomId)).willReturn(messages);
//
//        mockMvc.perform(get("/api/chat/rooms/" + roomId + "/messages")
//                        .principal(authentication))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("채팅 메시지 목록 조회 성공"))
//                .andExpect(jsonPath("$.data[0].message").value("test message"));
//    }
//
//    @Test
//    @DisplayName("채팅 메시지 송신")
//    void testSendChatMessage_success() throws Exception {
//        SendMessageRequestDto requestDto = new SendMessageRequestDto(roomId, senderId, "Hello Chat!");
//
//        ChatMessage savedMessage = ChatMessage.builder()
//                .id(UUID.randomUUID())
//                .chatRoomId(roomId)
//                .sender(user)
//                .content("Hello Chat!")
//                .sentAt(LocalDateTime.now())
//                .build();
//
//        given(chatService.sendChatMessage(any(SendMessageRequestDto.class))).willReturn(savedMessage);
//
//        mockMvc.perform(post("/api/chat/rooms/" + roomId + "/messages")
//                        .principal(authentication)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("채팅 메시지 전송 완료"))
//                .andExpect(jsonPath("$.data.message").value("Hello Chat!"));
//    }
//
//    @Test
//    @DisplayName("채팅방 나가기")
//    void testLeaveChatRoom_success() throws Exception {
//        mockMvc.perform(post("/api/chat/rooms/" + roomId + "/leave")
//                        .principal(authentication))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("채팅방 나가기 완료"));
//
//        then(chatRoomService).should().leaveChatRoom(eq(roomId), eq(senderId));
//    }
//}
//
//
//    @Test
//    @DisplayName("성공 (테스트 코드만 수정)")
//    void testCreateChatRoom() throws Exception {
//        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto(chatRoom.getPostId());
//
//        // 인증된 사용자의 이메일 반환하도록 설정
//        given(authentication.getName()).willReturn("user3@example.com");
//
//        // userRepository 의 email 조회도 설정
//        given(userRepository.findByEmail("user3@example.com")).willReturn(Optional.of(user));
//
//        // 채팅방 생성 서비스 모킹
//        given(chatRoomService.createOrGetRoom(any(CreateChatRoomRequestDto.class), any(UUID.class)))
//                .willReturn(chatRoom);
//
//        mockMvc.perform(post("/api/chat/room")
//                        .principal(authentication)   // 인증 목 객체 주입
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("채팅 요청 완료"))
//                .andExpect(jsonPath("$.data.roomId").value(roomId.toString()));
//    }