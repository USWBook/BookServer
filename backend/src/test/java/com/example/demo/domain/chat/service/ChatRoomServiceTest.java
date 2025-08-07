package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.entity.ChatRoomUser;
import com.example.demo.domain.chat.exception.ChatAccessDeniedException;
import com.example.demo.domain.chat.exception.ChatRoomNotFoundException;
import com.example.demo.domain.chat.exception.InvalidChatArgumentException;
import com.example.demo.domain.chat.repository.ChatMessageRepository;
import com.example.demo.domain.chat.repository.ChatRoomUserRepository;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOpsChatRoom;

    @Mock
    private ChatRoomUserRepository chatRoomUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private UUID postId;
    private UUID buyerId;
    private UUID sellerId;
    private Post post;
    private User sellerUser;
    private User buyerUser;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        buyerId = UUID.randomUUID();
        sellerId = UUID.randomUUID();

        post = Post.builder().id(postId).seller(null).build(); // 기본, seller는 테스트 내에서 변경
        sellerUser = User.builder().id(sellerId).build();
        buyerUser = User.builder().id(buyerId).build();

        // RedisTemplate opsForHash 리턴값 지정
        given(redisTemplate.opsForHash()).willReturn(hashOpsChatRoom);

        // chatRoomService에서 redisTemplate.opsForHash() 초기화 위해 init() 호출 필요
        chatRoomService.init();
    }

    // --- createOrGetRoom 관련 테스트 ---

    @Test
    @DisplayName("채팅방 생성 실패 - postId null 이면 IllegalArgumentException")
    void createOrGetRoom_postIdNull_throws() {
        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(null);

        assertThatThrownBy(() -> chatRoomService.createOrGetRoom(dto, buyerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("postId는 필수입니다.");
    }

    @Test
    @DisplayName("채팅방 생성 실패 - 게시글 없는 경우 IllegalArgException")
    void createOrGetRoom_postNotFound_throws() {
        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId);

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.createOrGetRoom(dto, buyerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글이 없습니다.");
    }

    @Test
    @DisplayName("신규 채팅방 생성")
    void createOrGetRoom_newRoom_success() {
        CreateChatRoomRequestDto dto = new CreateChatRoomRequestDto(postId);

        post = Post.builder().id(postId).seller(sellerUser).build();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).willReturn(Collections.emptyList());

        given(userRepository.findById(sellerId)).willReturn(Optional.of(sellerUser));
        given(userRepository.findById(buyerId)).willReturn(Optional.of(buyerUser));

        given(chatRoomUserRepository.save(any(ChatRoomUser.class))).willAnswer(invocation -> invocation.getArgument(0));

        ChatRoom created = chatRoomService.createOrGetRoom(dto, buyerId);

        // 필수 필드 확인
        assertThat(created.getPostId()).isEqualTo(postId);
        assertThat(created.getSender()).isIn(buyerId, sellerId);
        assertThat(created.getReceiver()).isIn(buyerId, sellerId);

        // Redis 캐시에 저장되었는지
        then(hashOpsChatRoom).should().put(eq(ChatRoomService.CHAT_ROOMS), eq(created.getRoomId().toString()), eq(created));

        // 멤버 저장 2회 확인
        then(chatRoomUserRepository).should(times(2)).save(any(ChatRoomUser.class));
    }

    // --- isSameParticipants() 관련 테스트 ---

    @Test
    @DisplayName("채팅방 참여 여부")
    void isSameParticipants_cases() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setSender(userA);
        room.setReceiver(userB);

        // 정상: 두 유저가 순서 달라도 true
        assertThat(chatRoomService.isSameParticipants(room, userA, userB)).isTrue();
        assertThat(chatRoomService.isSameParticipants(room, userB, userA)).isTrue();

        // sender null 시 예외 발생
        room.setSender(null);

        assertThatThrownBy(() -> chatRoomService.isSameParticipants(room, userA, userB))
                .isInstanceOf(InvalidChatArgumentException.class);
    }

    // --- findRoomByUser 테스트 ---

    @Test
    @DisplayName("참가자 맞고 삭제 상태 false인 채팅방만 반환")
    void findRoomByUser_filtersProperly() {
        UUID userId = UUID.randomUUID();

        ChatRoom room1 = new ChatRoom(UUID.randomUUID(), userId, UUID.randomUUID());
        ChatRoom room2 = new ChatRoom(UUID.randomUUID(), UUID.randomUUID(), userId);
        ChatRoom room3 = new ChatRoom(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        // userId 삭제 상태 true면 제외
        room2.setDeleteStatus(userId.toString(), true);

        given(hashOpsChatRoom.values(ChatRoomService.CHAT_ROOMS)).willReturn(List.of(room1, room2, room3));

        List<ChatRoom> found = chatRoomService.findRoomByUser(userId);

        // room1만 결과
        assertThat(found).containsExactly(room1);
    }

    // --- findByRoomId 테스트 ---

    @Test
    @DisplayName("채팅방 조회 정상, 없으면 예외")
    void findByRoomId_works() {
        UUID roomId = UUID.randomUUID();
        ChatRoom room = new ChatRoom();
        room.setRoomId(roomId);

        given(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).willReturn(room);

        ChatRoom found = chatRoomService.findByRoomId(roomId);
        assertThat(found).isEqualTo(room);

        given(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).willReturn(null);

        assertThatThrownBy(() -> chatRoomService.findByRoomId(roomId))
                .isInstanceOf(ChatRoomNotFoundException.class);
    }

    // --- deleteChatRoom 테스트 ---

    @Test
    @DisplayName("참가자 삭제 처리 및 양측 삭제시 Redis 삭제")
    void deleteChatRoom_works() {
        UUID roomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setRoomId(roomId);
        room.setSender(senderId);
        room.setReceiver(receiverId);

        room.setDeleteStatus(senderId.toString(), false);
        room.setDeleteStatus(receiverId.toString(), false);

        given(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).willReturn(room);

        // 첫 참가자 삭제 요청
        chatRoomService.deleteChatRoom(roomId, senderId);
        assertThat(room.getDeleteStatus(senderId.toString())).isTrue();
        then(hashOpsChatRoom).should().put(ChatRoomService.CHAT_ROOMS, roomId.toString(), room);
        then(hashOpsChatRoom).should(never()).delete(ChatRoomService.CHAT_ROOMS, roomId.toString());

        // 두번째 참가자 삭제 요청
        room.setDeleteStatus(receiverId.toString(), true);
        chatRoomService.deleteChatRoom(roomId, receiverId);

        // 양측 삭제 상태가 true면 Redis 에서 제거
        then(hashOpsChatRoom).should().delete(ChatRoomService.CHAT_ROOMS, roomId.toString());
    }

    // --- leaveChatRoom는 deleteChatRoom 호출 ---

    @Test
    @DisplayName("채팅방 나가기 및 삭제")
    void leaveChatRoom_callsDelete() {
        UUID roomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChatRoomService spyService = Mockito.spy(chatRoomService);

        doNothing().when(spyService).deleteChatRoom(roomId, userId);

        spyService.leaveChatRoom(roomId, userId);

        verify(spyService, times(1)).deleteChatRoom(roomId, userId);
    }

    // --- findOther 테스트 ---

    @Test
    @DisplayName("상대 유저 반환 및 권한 예외")
    void findOther_worksAndThrows() {
        UUID roomId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        ChatRoom room = new ChatRoom();
        room.setRoomId(roomId);
        room.setSender(senderId);
        room.setReceiver(receiverId);

        given(hashOpsChatRoom.get(ChatRoomService.CHAT_ROOMS, roomId.toString())).willReturn(room);

        UUID other1 = chatRoomService.findOther(roomId, senderId);
        assertThat(other1).isEqualTo(receiverId);

        UUID other2 = chatRoomService.findOther(roomId, receiverId);
        assertThat(other2).isEqualTo(senderId);

        UUID invalidUser = UUID.randomUUID();
        assertThatThrownBy(() -> chatRoomService.findOther(roomId, invalidUser))
                .isInstanceOf(ChatAccessDeniedException.class);
    }
}
