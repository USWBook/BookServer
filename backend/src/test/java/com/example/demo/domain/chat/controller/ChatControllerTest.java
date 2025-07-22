package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ChatRoomService chatRoomService;
    @MockBean
    ChatService chatService;

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
                .sender(buyerId)
                .receiver(sellerId)
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
    @DisplayName("내 채팅방 목록 조회")
    @WithMockUser
    void listRooms() throws Exception {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .sender(buyerId)
                .receiver(sellerId)
                .build();

        when(chatRoomService.findRoomByUser(eq(buyerId)))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/chat/rooms").param("userId", buyerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].roomId").value(roomId.toString()));
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
        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @DisplayName("채팅 메시지 목록 조회 - 200 OK")
    @WithMockUser
    void getMessages_success() throws Exception {
        ChatMessage msg = ChatMessage.builder()
                .roomId(roomId)
                .sender(buyerId)
                .receiver(sellerId)
                .message("테스트메시지")
                .build();
        when(chatService.getMessages(roomId))
                .thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chat/room/" + roomId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].message").value("테스트메시지"));
    }

    @Test
    @DisplayName("채팅방 하나 조회 성공/실패")
    @WithMockUser
    void getRoom_success_fail() throws Exception {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .sender(buyerId)
                .receiver(sellerId)
                .build();

        when(chatRoomService.findByRoomId(roomId)).thenReturn(room);

        // 성공 응답
        mockMvc.perform(get("/api/chat/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.roomId").value(roomId.toString()));

        // 실패 응답 (없는 방)
        when(chatRoomService.findByRoomId(any(UUID.class))).thenReturn(null);

        mockMvc.perform(get("/api/chat/room/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    @DisplayName("이미지 메시지 전송 - 200 OK")
    @WithMockUser
    void sendImage_success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "fake image content".getBytes());

        ChatMessage imgMsg = ChatMessage.builder()
                .roomId(roomId)
                .sender(buyerId)
                .receiver(sellerId)
                .image("https://url/test.jpg")
                .type(ChatMessage.MessageType.IMAGE)
                .build();

        when(chatService.sendImage(eq(roomId), eq(buyerId), eq(sellerId), any()))
                .thenReturn(imgMsg);

        mockMvc.perform(multipart("/api/chat/room/{roomId}/image", roomId)
                        .file(image)
                        .param("sender", buyerId.toString())
                        .param("receiver", sellerId.toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.image").value("https://url/test.jpg"));
    }

    @Test
    @DisplayName("채팅방 삭제 - 200 OK")
    @WithMockUser
    void deleteChatRoom_success() throws Exception {
        mockMvc.perform(delete("/api/chat/room/{roomId}", roomId)
                        .param("username", "testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("채팅 메시지 읽음 처리 - 200 OK")
    @WithMockUser
    void markMessagesRead_success() throws Exception {
        mockMvc.perform(post("/api/chat/room/{roomId}/read", roomId)
                        .param("username", "testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("메시지 읽음 처리 완료"));
    }
}
