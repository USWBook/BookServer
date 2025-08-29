//package com.example.demo.domain.chat.service;
//
//import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.entity.ChatRoomUser;
//import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
//import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
//import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
//import com.example.demo.domain.chat.repository.ChatMessageRepository;
//import com.example.demo.domain.chat.repository.ChatRoomUserRepository;
//import com.example.demo.domain.post.entity.Post;
//import com.example.demo.domain.post.repository.PostRepository;
//import com.example.demo.domain.report.entity.UserReport;
//import com.example.demo.domain.report.enums.ReportReason;
//import com.example.demo.domain.report.repository.UserReportRepository;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class ChatRoomServiceTest {
//
//    @Mock private RedisTemplate<String, Object> redisTemplate;
//    @Mock private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
//    @Mock private ChatRoomUserRepository chatRoomUserRepository;
//    @Mock private UserReportRepository userReportRepository;
//    @Mock private UserRepository userRepository;
//    @Mock private PostRepository postRepository;
//    @Mock private ChatMessageRepository chatMessageRepository;
//
//    @InjectMocks private ChatRoomService chatRoomService;
//
//    private UUID buyerId;
//    private UUID sellerId;
//    private UUID postId;
//    private Post post;
//    private User buyer;
//    private User seller;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        chatRoomService.init(); // PostConstruct 수동 호출
//
//        buyerId = UUID.randomUUID();
//        sellerId = UUID.randomUUID();
//        postId = UUID.randomUUID();
//
//        buyer = User.builder().id(buyerId).build();
//        seller = User.builder().id(sellerId).build();
//        post = Post.builder().id(postId).seller(seller).build();
//
//        when(redisTemplate.opsForHash()).thenReturn((HashOperations) hashOpsChatRoom);
//
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 생성 성공")
//    void createRoom_success() {
//        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId);
//
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
//        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
//        when(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).thenReturn(new ArrayList<>());
//
//        ChatRoom chatRoom = chatRoomService.createOrGetRoom(dto, buyerId);
//
//        assertNotNull(chatRoom);
//        verify(chatRoomUserRepository, times(2)).save(any(ChatRoomUser.class));
//        verify(hashOpsChatRoom).put(eq(ChatRoomService.CHAT_ROOMS), anyString(), eq(chatRoom));
//    }
//
//    @Test
//    @DisplayName("✅ 동일 유저+게시글 조합 -> 기존 채팅방 반환")
//    void createRoom_returnsExistingRoom() {
//        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId);
//
//        ChatRoom existing = new ChatRoom(postId, buyerId, sellerId);
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//        when(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).thenReturn(List.of(existing));
//
//        ChatRoom result = chatRoomService.createOrGetRoom(dto, buyerId);
//
//        assertEquals(existing, result);
//        verify(chatRoomUserRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("🚨 참가자 null이면 InvalidChatArgumentException")
//    void isSameParticipants_invalidArgument() {
//        ChatRoom room = new ChatRoom(postId, null, sellerId);
//
//        assertThrows(InvalidChatArgumentException.class,
//                () -> chatRoomService.isSameParticipants(room, buyerId, sellerId));
//    }
//
//    @Test
//    @DisplayName("✅ 유저 신고 성공")
//    void reportUser_success() {
//        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
//        UUID roomId = chatRoom.getRoomId();
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(chatRoom);
//        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
//        when(userReportRepository.save(any(UserReport.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserReport report = chatRoomService.reportUserByRoom(roomId, buyer, ReportReason.광고);
//
//        assertEquals(seller, report.getReported());
//        assertEquals(buyer, report.getReporter());
//    }
//
//    @Test
//    @DisplayName("🚨 채팅방 없을 때 ChatRoomNotFoundException")
//    void findByRoomId_notFound_throws() {
//        UUID roomId = UUID.randomUUID();
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(null);
//
//        assertThrows(ChatRoomNotFoundException.class,
//                () -> chatRoomService.findByRoomId(roomId));
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 삭제 시 soft delete → 양쪽 삭제 시 complete delete")
//    void deleteChatRoom_success() {
//        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
//        UUID roomId = chatRoom.getRoomId();
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(chatRoom);
//
//        // 첫 번째 사용자 삭제
//        chatRoomService.deleteChatRoom(roomId, buyerId);
//        verify(hashOpsChatRoom).put(eq(ChatRoomService.CHAT_ROOMS), eq(roomId.toString()), eq(chatRoom));
//
//        // 두 번째 사용자 삭제 -> 완전 삭제
//        chatRoom.setDeleteStatus(buyerId.toString(), true);
//        chatRoomService.deleteChatRoom(roomId, sellerId);
//        verify(hashOpsChatRoom).delete(ChatRoomService.CHAT_ROOMS, roomId.toString());
//    }
//
//    @Test
//    @DisplayName("🚨 findOther() - 참가자가 아니면 예외 발생")
//    void findOther_notParticipant_throws() {
//        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
//        UUID roomId = chatRoom.getRoomId();
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(chatRoom);
//
//        assertThrows(ChatAccessDeniedException.class,
//                () -> chatRoomService.findOther(roomId, UUID.randomUUID()));
//    }
//
//    @Test
//    @DisplayName("✅ leaveChatRoom → 마지막 유저 나가면 Redis에서 삭제")
//    void leaveChatRoom_removeRoomCompletely() {
//        ChatRoom chatRoom = new ChatRoom(postId, buyerId, sellerId);
//        UUID roomId = chatRoom.getRoomId();
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(chatRoom);
//
//        // 첫 번째 나감
//        chatRoomService.leaveChatRoom(roomId, buyerId);
//        verify(hashOpsChatRoom).put(eq(ChatRoomService.CHAT_ROOMS), eq(roomId.toString()), eq(chatRoom));
//
//        // 두 번째 나감 (최종 삭제)
//        // getUserCount == 0 조건
//        chatRoom.setDeleteStatus(buyerId.toString(), true);
//        chatRoom.setDeleteStatus(sellerId.toString(), true);
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString()))
//                .thenReturn(chatRoom);
//
//        chatRoomService.leaveChatRoom(roomId, sellerId);
//        verify(hashOpsChatRoom).delete(ChatRoomService.CHAT_ROOMS, roomId.toString());
//    }
//}
