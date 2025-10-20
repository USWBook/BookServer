//package com.example.demo.domain.chat.controller;
//
//import com.example.demo.domain.chat.dto.request.*;
//import com.example.demo.domain.chat.dto.response.*;
//import com.example.demo.domain.chat.entity.ChatMessage;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.service.ChatRoomService;
//import com.example.demo.domain.chat.service.ChatService;
//import com.example.demo.domain.post.entity.Post;
//import com.example.demo.domain.post.repository.PostRepository;
//import com.example.demo.domain.report.entity.UserReport;
//import com.example.demo.domain.report.enums.ReportReason;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//import com.example.demo.global.response.Empty;
//import com.example.demo.global.response.RsData;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class ChatControllerTest {
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
//    @Mock
//    private SecurityContext securityContext;
//
////    @InjectMocks
////    private ChatController chatController;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        SecurityContextHolder.setContext(securityContext);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 생성 성공")
//    void requestChatRoom_success() {
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//        UUID postId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//
//        User user = mockUserWithId(userId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        CreateChatRoomRequestDto dto = mock(CreateChatRoomRequestDto.class);
//        when(dto.postId()).thenReturn(postId);
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        UUID roomId = UUID.randomUUID();
//        when(chatRoom.getRoomId()).thenReturn(roomId);
//        when(chatRoom.getPostId()).thenReturn(postId);
//
//        when(chatRoomService.createOrGetRoom(dto, userId)).thenReturn(chatRoom);
//
//        Post post = mock(Post.class);
//        when(post.getTitle()).thenReturn("게시글 제목");
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//
//        // RsData 모킹
//        RsData<CreateChatRoomResponseDto> rsData = mock(RsData.class);
//        CreateChatRoomResponseDto createDto = mock(CreateChatRoomResponseDto.class);
//
//        when(rsData.getCode()).thenReturn("200");
//        when(rsData.getMessage()).thenReturn("채팅 요청 완료");
//        when(rsData.getData()).thenReturn(createDto);
//
//        when(createDto.roomId()).thenReturn(roomId);
//        when(createDto.title()).thenReturn("게시글 제목");
//
//        // 실제 메서드 호출 대신 서비스 메서드 동작 모킹 안 하고 바로 반환 테스트도 가능
//        RsData<CreateChatRoomResponseDto> response = chatController.requestChatRoom(dto);
//
//        // RsData는 실제 반환 객체이므로 필드 직접 검증이 아니라 서비스 호출이나 상태 검증이 현실적
//        assertNotNull(response);
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 입장 성공")
//    void enterChatRoom_success() {
//        UUID roomId = UUID.randomUUID();
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUserWithId(userId)));
//
//        ChatRoom room = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(room);
//        when(room.getSender()).thenReturn(userId);
//        when(room.getReceiver()).thenReturn(UUID.randomUUID());
//        when(room.getRoomId()).thenReturn(roomId);
//        when(room.getPostId()).thenReturn(UUID.randomUUID());
//        when(room.getName()).thenReturn("채팅방 이름");
//        when(room.getUserCount()).thenReturn(2);
//        when(room.getLastMessage()).thenReturn("안녕하세요");
//        when(room.getLastTimestamp()).thenReturn(LocalDateTime.now().toString());
//
//        RsData<EnterChatRoomResponseDto.Data> response = chatController.enterChatRoom(roomId, authentication);
//
//        assertNotNull(response);
//        assertEquals("200", response.getCode());
//        assertEquals("채팅방 입장 성공", response.getMessage());
//        assertNotNull(response.getData());
//        assertEquals(roomId.toString(), response.getData().roomId());
//    }
//
//    @Test
//    @DisplayName("✅ 내 채팅방 목록 조회 성공")
//    void listRooms_success() {
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//
//        User user = new User();
//        user.setId(userId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoom.getRoomId()).thenReturn(UUID.randomUUID());
//        when(chatRoom.getPostId()).thenReturn(UUID.randomUUID());
//        when(chatRoom.getName()).thenReturn("채팅방1");
//        when(chatRoom.getUserCount()).thenReturn(2);
//        when(chatRoom.getLastMessage()).thenReturn("안녕하세요");
//        when(chatRoom.getLastTimestamp()).thenReturn(LocalDateTime.now().toString());
//
//        when(chatRoomService.findRoomByUser(userId)).thenReturn(List.of(chatRoom));
//
//        RsData<List<ListChatRoomsResponseDto.ChatRoomDto>> result = chatController.listRooms(authentication);
//
//        assertEquals("200", result.getCode());
//        assertEquals("나의 채팅방 목록 조회 성공", result.getMessage());
//        assertFalse(result.getData().isEmpty());
//        assertEquals("채팅방1", result.getData().get(0).name());
//    }
//
//    @Test
//    @DisplayName("✅ 채팅 메시지 목록 조회 성공")
//    void getMessages_success() {
//        UUID roomId = UUID.randomUUID();
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        User user = new User();
//        user.setId(userId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findByRoomId(roomId)).thenReturn(chatRoom);
//        when(chatRoom.getSender()).thenReturn(userId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        ChatMessage message = mock(ChatMessage.class);
//        when(message.getId()).thenReturn(UUID.randomUUID());
//        when(message.getChatRoomId()).thenReturn(roomId);
//        User senderUser = new User();
//        senderUser.setId(UUID.randomUUID());
//        when(message.getSender()).thenReturn(senderUser);
//        when(message.getContent()).thenReturn("테스트 메시지");
//        when(message.getSentAt()).thenReturn(LocalDateTime.now());
//
//        when(chatService.getMessages(roomId)).thenReturn(List.of(message));
//
//        //리시브수정으로 인해 주석처리
//        //RsData<List<SendMessageResponseDto.Data>> result = chatController.getMessages(roomId, authentication);
//
////        assertEquals("200", result.getCode());
////        assertEquals("채팅 메시지 목록 조회 성공", result.getMessage());
////        assertEquals(1, result.getData().size());
////        assertEquals("테스트 메시지", result.getData().get(0).message());
//    }
//
//    @Test
//    @DisplayName("✅ 채팅 메시지 전송 성공")
//    void sendChatMessage_success() {
//        String email = "user@example.com";
//        UUID senderId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//        UUID messageId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        User senderUser = new User();
//        senderUser.setId(senderId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(senderUser));
//
//        SendMessageRequestDto requestDto = mock(SendMessageRequestDto.class);
//        when(requestDto.roomId()).thenReturn(roomId);
//
//        ChatMessage savedMessage = mock(ChatMessage.class);
//        when(savedMessage.getId()).thenReturn(messageId);
//        when(savedMessage.getChatRoomId()).thenReturn(roomId);
//        when(savedMessage.getSender()).thenReturn(senderUser);
//        when(savedMessage.getContent()).thenReturn("내용");
//        when(savedMessage.getSentAt()).thenReturn(LocalDateTime.now());
//
//        when(chatService.sendChatMessage(requestDto, senderId)).thenReturn(savedMessage);
//
//        RsData<SendMessageResponseDto.Data> result = chatController.sendChatMessage(requestDto, authentication);
//
//        assertEquals("200", result.getCode());
//        assertEquals("채팅 메시지 전송 완료", result.getMessage());
//        assertEquals(messageId, result.getData().messageId());
//        assertEquals(roomId, result.getData().roomId());
//    }
//
//    @Test
//    @DisplayName("✅ 이미지 메시지 전송 성공")
//    void sendImageMessage_success() {
//        String email = "user@example.com";
//        UUID senderId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//        UUID messageId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        User senderUser = new User();
//        senderUser.setId(senderId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(senderUser));
//
//        SendImageRequestDto requestDto = mock(SendImageRequestDto.class);
//        when(requestDto.roomId()).thenReturn(roomId);
//
//        ChatMessage imageMessage = mock(ChatMessage.class);
//        when(imageMessage.getId()).thenReturn(messageId);
//        when(imageMessage.getChatRoomId()).thenReturn(roomId);
//        when(imageMessage.getSender()).thenReturn(senderUser);
//        when(imageMessage.getImageUrl()).thenReturn("http://example.com/image.png");
//        when(imageMessage.getSentAt()).thenReturn(LocalDateTime.now());
//
//        when(chatService.sendImageMessage(requestDto, senderId)).thenReturn(imageMessage);
//
//        RsData<SendImageResponseDto.Data> result = chatController.sendImageMessage(requestDto, authentication);
//
//        assertEquals("200", result.getCode());
//        assertEquals("이미지 메시지 전송 완료", result.getMessage());
//        assertEquals(messageId, result.getData().messageId());
//    }
////
////    @Test
////    @DisplayName("✅ 유저 신고 성공")
////    void reportUser_success() {
////        String email = "user@example.com";
////        UUID reporterId = UUID.randomUUID();
////        UUID roomId = UUID.randomUUID();
////        UUID reportId = UUID.randomUUID();
////        UUID reportedUserId = UUID.randomUUID();
////
////        when(authentication.getName()).thenReturn(email);
////        User reporter = new User();
////        reporter.setId(reporterId);
////        when(userRepository.findByEmail(email)).thenReturn(Optional.of(reporter));
////
////        UserReport report = mock(UserReport.class);
////        when(report.getId()).thenReturn(reportId);
////        when(report.getReporter()).thenReturn(reporter);
////        User reported = new User();
////        reported.setId(reportedUserId);
////        when(report.getReported()).thenReturn(reported);
////        when(report.getReason()).thenReturn(ReportReason.광고);
////        when(report.getReportedAt()).thenReturn(LocalDateTime.now());
////
////        when(chatRoomService.reportUserByRoom(roomId, reporter, ReportReason.광고)).thenReturn(report);
////
////        ReportUserRequestDto requestDto = mock(ReportUserRequestDto.class);
////        when(requestDto.reason()).thenReturn(ReportReason.광고);
////
////        RsData<ReportUserResponseDto> result = chatController.reportUser(roomId, requestDto, authentication);
////
////        assertEquals("200", result.getCode());
////        assertEquals("신고 완료", result.getMessage());
////        assertEquals(reportId, result.getData().reportId());
////        assertEquals(roomId, result.getData().roomId());
////    }
//
//    @Test
//    @DisplayName("✅ 채팅방 나가기 성공")
//    void leaveChatRoom_success() {
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        User user = new User();
//        user.setId(userId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        LeaveChatRoomRequestDto requestDto = mock(LeaveChatRoomRequestDto.class);
//        when(requestDto.roomId()).thenReturn(roomId);
//
//        doNothing().when(chatRoomService).leaveChatRoom(roomId, userId);
//        when(chatRoomService.getUserCount(roomId)).thenReturn(1);
//
//        RsData<LeaveChatRoomResponseDto> result = chatController.leaveChatRoom(requestDto, authentication);
//
//        assertEquals("200", result.getCode());
//        assertEquals("채팅방 나가기 완료", result.getMessage());
//        assertEquals(1, result.getData().userCount());
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 삭제 성공")
//    void removeChatRoom_success() {
//        String email = "user@example.com";
//        UUID userId = UUID.randomUUID();
//        UUID roomId = UUID.randomUUID();
//
//        when(authentication.getName()).thenReturn(email);
//        User user = new User();
//        user.setId(userId);
//        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
//
//        LeaveChatRoomRequestDto requestDto = mock(LeaveChatRoomRequestDto.class);
//        when(requestDto.roomId()).thenReturn(roomId);
//
//        doNothing().when(chatRoomService).leaveChatRoom(roomId, userId);
//        when(chatRoomService.getUserCount(roomId)).thenReturn(0);
//        doNothing().when(chatRoomService).deleteChatRoomFromDb(roomId);
//        doNothing().when(chatRoomService).deleteChatRoomFromRedis(roomId);
//
//        RsData<Empty> result = chatController.removeChatRoom(requestDto, authentication);
//
//        assertEquals("200", result.getCode());
//        assertEquals("채팅방 완전 삭제 처리 완료", result.getMessage());
//        assertNull(result.getData());
//    }
//
//    private User mockUserWithId(UUID id) {
//        User user = new User();
//        user.setId(id);
//        return user;
//    }
//}
