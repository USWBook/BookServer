package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false) // JWT, CSRF 등 Security 필터 비활성화
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RedisTokenRepository redisTokenRepository;

    final UUID postId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final UUID sellerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    final UUID buyerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    final UUID roomId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");


    @Test
    @DisplayName("채팅방 생성 - 200 OK")
    @WithMockUser
    void createRoom_success() throws Exception {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .postId(postId)
                .sender(buyerId)
                .receiver(sellerId)
                .userCount(2)
                .build();

        when(chatRoomService.createOrGetRoom(any(CreateChatRoomRequestDto.class)))
                .thenReturn(room);

        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto(postId, sellerId, buyerId);

        mockMvc.perform(post("/api/chat/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.data.roomId").value(roomId.toString()));
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 - 200 OK")
    @WithMockUser
    void listRooms() throws Exception {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .postId(postId)
                .sender(buyerId)
                .receiver(sellerId)
                .userCount(2)
                .lastMessage("안녕하세요")
                .lastTimestamp("2023-01-01T12:00:00")
                .build();

        when(chatRoomService.findRoomByUser(eq(buyerId)))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/chat/rooms").param("userId", buyerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.data[0].roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.data.data[0].lastMessage").value("안녕하세요"));
    }

    @Test
    @DisplayName("채팅 메시지 전송 API - 200 OK")
    @WithMockUser
    void sendChatMessage_success() throws Exception {
        SendMessageRequestDto request = new SendMessageRequestDto(
                roomId,
                buyerId,
                UUID.randomUUID(),
                sellerId,
                "hello!"
        );

        User senderUser = User.builder()
                .id(buyerId)
                .email("user@example.com")
                .name("테스트유저")
                .build();

        ChatMessage savedMessage = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoomId(roomId)
                .sender(senderUser) // sender User 객체는 서비스 레이어에서 채워짐, 여기서는 null 허용
                .content("hello!")
                .sentAt(LocalDateTime.now())
                .build();

        when(chatService.sendChatMessage(any(SendMessageRequestDto.class)))
                .thenReturn(savedMessage);

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.data.message").value("hello!"));
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 - 200 OK")
    @WithMockUser
    void getMessages_success() throws Exception {

        User senderUser = User.builder()
                .id(buyerId)
                .email("testuser@example.com")
                .name("테스트유저")
                .build();

        ChatMessage msg = ChatMessage.builder()
                .id(UUID.randomUUID())
                .chatRoomId(roomId)
                .sender(senderUser) // 테스트용 null 가능
                .content("테스트메시지")
                .sentAt(LocalDateTime.now())
                .build();

        when(chatService.getMessages(roomId))
                .thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chat/room/" + roomId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].message").value("테스트메시지"));
    }

    @Test
    @DisplayName("채팅방 하나 조회 성공")
    @WithMockUser
    void getRoom_success() throws Exception {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .postId(postId)
                .sender(buyerId)
                .receiver(sellerId)
                .userCount(2)
                .build();

        when(chatRoomService.findByRoomId(roomId)).thenReturn(room);

        mockMvc.perform(get("/api/chat/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.roomId").value(roomId.toString()));
    }

//    @Test
//    @DisplayName("채팅방 삭제 - 200 OK")
//    @WithMockUser
//    void deleteChatRoom_success() throws Exception {
//        mockMvc.perform(delete("/api/chat/room/{roomId}", roomId)
//                        .param("username", "testuser")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value("200"));
//        // 필요시 verify(chatRoomService).deleteChatRoom(roomId, "testuser")
//    }

//    @Test
//    @DisplayName("채팅 메시지 읽음 처리 - 200 OK")
//    @WithMockUser
//    void markMessagesRead_success() throws Exception {
//        // 실제 서비스 호출은 MockBean에서 동작 모방
//
//        mockMvc.perform(post("/api/chat/room/{roomId}/read", roomId)
//                        .param("username", "testuser")
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value("200"))
//                .andExpect(jsonPath("$.message").value("메시지 읽음 처리 완료"));
//    }
}
