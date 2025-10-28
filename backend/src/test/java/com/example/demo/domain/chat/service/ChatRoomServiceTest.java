//package com.example.demo.domain.chat.service;
//
//import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import com.example.demo.domain.chat.entity.ChatRoomUser;
//import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
//import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
//import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
//import com.example.demo.domain.chat.repository.ChatRoomUserRepository;
//import com.example.demo.domain.chat.repository.ChatMessageRepository;
//import com.example.demo.domain.post.entity.Post;
//import com.example.demo.domain.post.repository.PostRepository;
//import com.example.demo.domain.report.entity.UserReport;
//import com.example.demo.domain.report.enums.ReportReason;
//import com.example.demo.domain.report.repository.UserReportRepository;
//import com.example.demo.domain.user.entity.User;
//import com.example.demo.domain.user.repository.UserRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class ChatRoomServiceTest {
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Mock
//    private HashOperations<String, Object, Object> hashOpsChatRoom;
//
//    @Mock
//    private ChatRoomUserRepository chatRoomUserRepository;
//
//    @Mock
//    private UserReportRepository userReportRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private ChatMessageRepository chatMessageRepository;
//
//    @Spy
//    @InjectMocks
//    private ChatRoomService chatRoomService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(redisTemplate.opsForHash()).thenReturn(hashOpsChatRoom);
//        chatRoomService.init();
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 생성 성공 - 기존 방 있으면 반환")
//    void createOrGetRoom_existingRoom_returnsExisting() {
//        UUID buyerId = UUID.randomUUID();
//        UUID sellerId = UUID.randomUUID();
//        UUID postId = UUID.randomUUID();
//
//        Post post = mock(Post.class);
//        User seller = mock(User.class);
//        when(post.getSeller()).thenReturn(seller);
//        when(seller.getId()).thenReturn(sellerId);
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//
//        ChatRoom existingRoom = new ChatRoom(postId, buyerId, sellerId);
//        when(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).thenReturn(List.of(existingRoom));
//        doReturn(true).when(chatRoomService).isSameParticipants(existingRoom, buyerId, sellerId);
//
//        CreateChatRoomRequestDto dto = mock(CreateChatRoomRequestDto.class);
//        when(dto.postId()).thenReturn(postId);
//
//        ChatRoom result = chatRoomService.createOrGetRoom(dto, buyerId);
//
//        assertEquals(existingRoom, result);
//        verify(hashOpsChatRoom).put(ChatRoomService.CHAT_ROOMS, existingRoom.getRoomId().toString(), existingRoom);
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 생성 성공 - 신규 방 생성")
//    void createOrGetRoom_newRoom_createsAndReturns() {
//        UUID buyerId = UUID.randomUUID();
//        UUID sellerId = UUID.randomUUID();
//        UUID postId = UUID.randomUUID();
//
//        Post post = mock(Post.class);
//        User seller = mock(User.class);
//        User buyer = mock(User.class);
//
//        when(post.getSeller()).thenReturn(seller);
//        when(seller.getId()).thenReturn(sellerId);
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//
//        when(userRepository.findById(sellerId)).thenReturn(Optional.of(seller));
//        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
//
//        when(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).thenReturn(Collections.emptyList());
//
//        CreateChatRoomRequestDto dto = mock(CreateChatRoomRequestDto.class);
//        when(dto.postId()).thenReturn(postId);
//
//        ChatRoom result = chatRoomService.createOrGetRoom(dto, buyerId);
//
//        assertNotNull(result);
//        assertEquals(postId, result.getPostId());
//        verify(hashOpsChatRoom).put(eq(ChatRoomService.CHAT_ROOMS), eq(result.getRoomId().toString()), any(ChatRoom.class));
//        verify(chatRoomUserRepository, times(2)).save(any(ChatRoomUser.class));
//    }
//
//    @Test
//    @DisplayName("⭕️ 채팅방 생성 실패 - postId null")
//    void createOrGetRoom_postIdNull_throws() {
//        CreateChatRoomRequestDto dto = mock(CreateChatRoomRequestDto.class);
//        when(dto.postId()).thenReturn(null);
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> chatRoomService.createOrGetRoom(dto, UUID.randomUUID()));
//        assertEquals("postId는 필수입니다.", ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("⭕️ 채팅방 생성 실패 - 게시글 없을 때")
//    void createOrGetRoom_postNotFound_throws() {
//        UUID postId = UUID.randomUUID();
//        CreateChatRoomRequestDto dto = mock(CreateChatRoomRequestDto.class);
//        when(dto.postId()).thenReturn(postId);
//        when(postRepository.findById(postId)).thenReturn(Optional.empty());
//
//        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> chatRoomService.createOrGetRoom(dto, UUID.randomUUID()));
//        assertTrue(ex.getMessage().contains("게시글이 없습니다"));
//    }
//
////    @Test
////    @DisplayName("✅ 유저 신고 성공")
////    void reportUserByRoom_success() {
////        UUID roomId = UUID.randomUUID();
////        UUID reporterId = UUID.randomUUID();
////        UUID otherUserId = UUID.randomUUID();
////
////        User reporter = mock(User.class);
////        when(reporter.getId()).thenReturn(reporterId);
////
////        ChatRoom chatRoom = mock(ChatRoom.class);
////        when(chatRoomService.findChatRoomById(roomId)).thenReturn(chatRoom);
////
////        when(chatRoom.getSender()).thenReturn(reporterId);
////        when(chatRoom.getReceiver()).thenReturn(otherUserId);
////
////        User reported = mock(User.class);
////        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(reported));
////
////        UserReport savedReport = mock(UserReport.class);
////        when(userReportRepository.save(any(UserReport.class))).thenReturn(savedReport);
////
////        UserReport report = chatRoomService.reportUserByRoom(roomId, reporter, ReportReason.광고);
////        assertNotNull(report);
////        verify(userReportRepository).save(any(UserReport.class));
////    }
//
//    @Test
//    @DisplayName("⭕️ 유저 신고 실패 - 채팅방 없음")
//    void reportUserByRoom_noRoom_throws() {
//        UUID roomId = UUID.randomUUID();
//        User reporter = mock(User.class);
//
//        doReturn(null).when(chatRoomService).findChatRoomById(roomId);
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatRoomService.reportUserByRoom(roomId, reporter, ReportReason.광고));
//        assertEquals("채팅방 없음", ex.getMessage());
//    }
//
//
//    @Test
//    @DisplayName("⭕️ 유저 신고 실패 - 신고 대상자 없음")
//    void reportUserByRoom_reportedUserNotFound_throws() {
//        UUID roomId = UUID.randomUUID();
//        User reporter = mock(User.class);
//        when(reporter.getId()).thenReturn(UUID.randomUUID());
//
//        ChatRoom chatRoom = mock(ChatRoom.class);
//        when(chatRoomService.findChatRoomById(roomId)).thenReturn(chatRoom);
//
//        UUID reporterId = UUID.randomUUID();
//        when(reporter.getId()).thenReturn(reporterId);
//        when(chatRoom.getSender()).thenReturn(reporterId);
//        when(chatRoom.getReceiver()).thenReturn(UUID.randomUUID());
//
//        when(userRepository.findById(any())).thenReturn(Optional.empty());
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> chatRoomService.reportUserByRoom(roomId, reporter, ReportReason.광고));
//        assertEquals("신고 대상자 없음", ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("✅ Redis에서 채팅방 조회 성공")
//    void findChatRoomById_returnsRoom() {
//        UUID roomId = UUID.randomUUID();
//        ChatRoom room = mock(ChatRoom.class);
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(room);
//
//        ChatRoom result = chatRoomService.findChatRoomById(roomId);
//        assertEquals(room, result);
//    }
//
//    @Test
//    @DisplayName("✅ 참여자 동일성 확인")
//    void isSameParticipants_checksCorrectly() {
//        UUID userA = UUID.randomUUID();
//        UUID userB = UUID.randomUUID();
//
//        ChatRoom room = mock(ChatRoom.class);
//        when(room.getSender()).thenReturn(userA);
//        when(room.getReceiver()).thenReturn(userB);
//
//        // 정상 케이스
//        assertTrue(chatRoomService.isSameParticipants(room, userA, userB));
//        assertTrue(chatRoomService.isSameParticipants(room, userB, userA));
//
//        // 잘못된 인자 null이면 예외 발생
//        when(room.getSender()).thenReturn(null);
//        assertThrows(InvalidChatArgumentException.class, () -> chatRoomService.isSameParticipants(room, userA, userB));
//    }
//
//    @Test
//    @DisplayName("✅ 특정 유저의 채팅방 목록 조회")
//    void findRoomByUser_returnsOnlyRoomsNotDeleted() {
//        UUID userId = UUID.randomUUID();
//
//        ChatRoom room1 = mock(ChatRoom.class);
//        when(room1.getSender()).thenReturn(userId);
//        when(room1.getReceiver()).thenReturn(UUID.randomUUID());
//        when(room1.getDeleteStatus(userId.toString())).thenReturn(false);
//
//        ChatRoom room2 = mock(ChatRoom.class);
//        when(room2.getSender()).thenReturn(UUID.randomUUID());
//        when(room2.getReceiver()).thenReturn(userId);
//        when(room2.getDeleteStatus(userId.toString())).thenReturn(true); // 삭제된 상태라 리스트에 안 들어감
//
//        when(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).thenReturn(List.of(room1, room2));
//
//        List<ChatRoom> result = chatRoomService.findRoomByUser(userId);
//        assertEquals(1, result.size());
//        assertTrue(result.contains(room1));
//    }
//
//    @Test
//    @DisplayName("✅ UUID로 채팅방 조회 - 있으면 반환")
//    void findByRoomId_exists() {
//        UUID roomId = UUID.randomUUID();
//        ChatRoom room = mock(ChatRoom.class);
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(room);
//
//        ChatRoom result = chatRoomService.findByRoomId(roomId);
//        assertEquals(room, result);
//    }
//
//    @Test
//    @DisplayName("⭕️ UUID로 채팅방 조회 - 없으면 예외")
//    void findByRoomId_notExists() {
//        UUID roomId = UUID.randomUUID();
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(null);
//
//        assertThrows(ChatRoomNotFoundException.class, () -> chatRoomService.findByRoomId(roomId));
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 삭제 처리 - soft delete 및 완전 삭제 조건 확인")
//    void deleteChatRoom_softAndHardDelete() {
//        UUID roomId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//        ChatRoom room = mock(ChatRoom.class);
//
//        doReturn(room).when(chatRoomService).findByRoomId(roomId);  // <-- doReturn() 으로 변경
//
//        when(room.getDeleteStatus(anyString())).thenReturn(false).thenReturn(true);
//        when(room.getSender()).thenReturn(userId);
//        when(room.getReceiver()).thenReturn(UUID.randomUUID());
//
//        chatRoomService.deleteChatRoom(roomId, userId);
//
//        verify(room).setDeleteStatus(userId.toString(), true);
//        verify(hashOpsChatRoom).put(ChatRoomService.CHAT_ROOMS, roomId.toString(), room);
//    }
//
//
//    @Test
//    @DisplayName("✅ 채팅방 나가기 - 유저 상태 변경, 완전 삭제 조건")
//    void leaveChatRoom_updatesDeleteStatus() {
//        UUID roomId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//        ChatRoom room = mock(ChatRoom.class);
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(room);
//        when(room.getDeleteStatus(anyString())).thenReturn(false);
//        when(room.getSender()).thenReturn(userId);
//        when(room.getReceiver()).thenReturn(UUID.randomUUID());
//
//        chatRoomService.leaveChatRoom(roomId, userId);
//
//        verify(room).setDeleteStatus(userId.toString(), true);
//        verify(hashOpsChatRoom).put(ChatRoomService.CHAT_ROOMS, roomId.toString(), room);
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 나가기 - 이미 방이 없으면 무시")
//    void leaveChatRoom_noRoom_noException() {
//        UUID roomId = UUID.randomUUID();
//        UUID userId = UUID.randomUUID();
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(null);
//
//        // 예외 없이 조용히 종료
//        assertDoesNotThrow(() -> chatRoomService.leaveChatRoom(roomId, userId));
//    }
//
//    @Test
//    @DisplayName("✅ 채팅방 내 남아있는 사용자 수 계산")
//    void getUserCount_countsCorrectly() {
//        UUID roomId = UUID.randomUUID();
//        ChatRoom room = mock(ChatRoom.class);
//
//        when(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).thenReturn(room);
//        UUID senderId = UUID.randomUUID();
//        UUID receiverId = UUID.randomUUID();
//
//        when(room.getSender()).thenReturn(senderId);
//        when(room.getReceiver()).thenReturn(receiverId);
//
//        when(room.getDeleteStatus(senderId.toString())).thenReturn(false);
//        when(room.getDeleteStatus(receiverId.toString())).thenReturn(true);
//
//        int count = chatRoomService.getUserCount(roomId);
//        assertEquals(1, count);
//    }
//
//    @Test
//    @DisplayName("✅ 상대방 UUID 조회 - 정상 조회")
//    void findOther_returnsOtherUserId() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//        UUID receiverId = UUID.randomUUID();
//
//        ChatRoom room = mock(ChatRoom.class);
//        doReturn(room).when(chatRoomService).findByRoomId(roomId);
//
//        when(room.getSender()).thenReturn(senderId);
//        when(room.getReceiver()).thenReturn(receiverId);
//
//        UUID other = chatRoomService.findOther(roomId, senderId);
//        assertEquals(receiverId, other);
//    }
//
//    @Test
//    @DisplayName("⭕️ 상대방 UUID 조회 - 권한 없으면 예외")
//    void findOther_noAuthority_throws() {
//        UUID roomId = UUID.randomUUID();
//        UUID senderId = UUID.randomUUID();
//
//        ChatRoom room = mock(ChatRoom.class);
//        doReturn(room).when(chatRoomService).findByRoomId(roomId);  // doReturn 패턴으로 변경
//
//        when(room.getSender()).thenReturn(UUID.randomUUID());
//        when(room.getReceiver()).thenReturn(UUID.randomUUID());
//
//        assertThrows(ChatAccessDeniedException.class, () -> chatRoomService.findOther(roomId, senderId));
//    }
//
//
//    @Test
//    @DisplayName("✅ DB에서 채팅방 완전 삭제")
//    void deleteChatRoomFromDb_callsRepository() {
//        UUID roomId = UUID.randomUUID();
//        chatRoomService.deleteChatRoomFromDb(roomId);
//        verify(chatRoomUserRepository).deleteByChatRoomId(roomId);
//    }
//
//    @Test
//    @DisplayName("✅ Redis에서 채팅방 완전 삭제")
//    void deleteChatRoomFromRedis_callsRedisDelete() {
//        UUID roomId = UUID.randomUUID();
//        chatRoomService.deleteChatRoomFromRedis(roomId);
//        verify(hashOpsChatRoom).delete(ChatRoomService.CHAT_ROOMS, roomId.toString());
//    }
//}
