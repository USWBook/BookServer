//package com.example.demo.domain.chat.service;
//
//import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
//import com.example.demo.domain.chat.entity.ChatRoom;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//class ChatRoomServiceTest {
//
//    @InjectMocks
//    private ChatRoomService chatRoomService;
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    // HashOperations의 타입은 실제 redisTemplate에서 반환하는 것과 일치하도록 Object로 맞추어야 Mock 오류가 없습니다.
//    @Mock
//    private HashOperations<String, Object, ChatRoom> hashOpsChatRoom;
//
//    private UUID postId;
//    private UUID sellerId;
//    private UUID buyerId;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // RedisTemplate의 opsForHash()를 반환하도록 세팅
//        // ChatRoomService 내부 필드에도 직접 주입 (테스트 환경에서 @PostConstruct 실행 안 됨)
//        ReflectionTestUtils.setField(chatRoomService, "hashOpsChatRoom", hashOpsChatRoom);
//
//        postId = UUID.randomUUID();
//        sellerId = UUID.randomUUID();
//        buyerId = UUID.randomUUID();
//    }
//
//    @Test
//    @DisplayName("채팅방이 없으면 신규 생성")
//    void createOrGetRoom_createsNew() {
//        // given
//        given(hashOpsChatRoom.values("CHAT_ROOM")).willReturn(new ArrayList<>());
//
//        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId, sellerId, buyerId);
//
//        // when
//        ChatRoom room = chatRoomService.createOrGetRoom(dto);
//
//        // then
//        assertThat(room.getPostId()).isEqualTo(postId);
//        assertThat(room.getSender()).isEqualTo(buyerId);
//        assertThat(room.getReceiver()).isEqualTo(sellerId);
//
//        verify(hashOpsChatRoom).put(eq("CHAT_ROOM"), eq(room.getRoomId().toString()), any());
//    }
//
//    @Test
//    @DisplayName("이미 존재하는 방이면 기존 방 반환 및 삭제 플래그 복구")
//    void createOrGetRoom_returnsExistingRestoresDeleteStatus() {
//        // given
//        ChatRoom existingRoom = ChatRoom.builder()
//                .roomId(UUID.randomUUID())
//                .postId(postId)
//                .sender(buyerId)
//                .receiver(sellerId)
//                .isDelete(new HashMap<>(Map.of(buyerId.toString(), true, sellerId.toString(), false)))
//                .build();
//
//        given(hashOpsChatRoom.values("CHAT_ROOM")).willReturn(List.of(existingRoom));
//
//        //CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId, sellerId, buyerId);
//
//        // when
//        //ChatRoom room = chatRoomService.createOrGetRoom(dto);
//
//        // then
//        assertThat(room).isSameAs(existingRoom);
//        assertThat(room.getDeleteStatus(buyerId.toString())).isFalse();
//        verify(hashOpsChatRoom).put("CHAT_ROOM", room.getRoomId().toString(), room);
//    }
//
//    @Test
//    @DisplayName("유저 채팅방 목록은 논리삭제 제외")
//    void findRoomByUser_excludesDeleted() {
//        // given
//        ChatRoom room1 = ChatRoom.builder().roomId(UUID.randomUUID())
//                .postId(postId).sender(buyerId).receiver(sellerId)
//                .isDelete(Map.of(buyerId.toString(), false)).build();
//
//        ChatRoom room2 = ChatRoom.builder().roomId(UUID.randomUUID())
//                .postId(postId).sender(buyerId).receiver(sellerId)
//                .isDelete(Map.of(buyerId.toString(), true)).build();
//
//        given(hashOpsChatRoom.values("CHAT_ROOM")).willReturn(List.of(room1, room2));
//
//        // when
//        List<ChatRoom> list = chatRoomService.findRoomByUser(buyerId);
//
//        // then
//        assertThat(list).containsExactly(room1);
//    }
//
//    @Test
//    @DisplayName("채팅방 ID로 조회 성공")
//    void findByRoomId_success() {
//        UUID roomId = UUID.randomUUID();
//        ChatRoom room = ChatRoom.builder().roomId(roomId).build();
//
//        given(hashOpsChatRoom.get("CHAT_ROOM", roomId.toString())).willReturn(room);
//
//        // when
//        ChatRoom found = chatRoomService.findByRoomId(roomId);
//
//        // then
//        assertThat(found).isSameAs(room);
//    }
//
//    @Test
//    @DisplayName("논리적 삭제 및 삭제")
//    void deleteChatRoom_deletesIfBothMarked() {
//        UUID roomId = UUID.randomUUID();
//        UUID userAId = UUID.randomUUID();
//        UUID userBId = UUID.randomUUID();
//        String userA = userAId.toString();
//        String userB = userBId.toString();
//
//        ChatRoom room = ChatRoom.builder()
//                .roomId(roomId)
//                .sender(userAId)
//                .receiver(userBId)
//                .isDelete(new HashMap<>(Map.of(userA, false, userB, true)))
//                .build();
//
//        given(hashOpsChatRoom.get("CHAT_ROOM", roomId.toString())).willReturn(room);
//
//        // when
//        //chatRoomService.deleteChatRoom(roomId, userA);
//
//        // then
//        assertThat(room.getDeleteStatus(userA)).isTrue();
//        verify(hashOpsChatRoom).put("CHAT_ROOM", roomId.toString(), room);
//        verify(hashOpsChatRoom).delete("CHAT_ROOM", roomId.toString());
//    }
//
//}
