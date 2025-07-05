package com.example.demo.domain.post.controller;

import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PostRepository postRepository;

    private Member createMockMember(UUID id) {
        return Member.builder()
                .id(id)
                .email("test@example.com")
                .password("password")
                .name("테스트유저")
                .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "제목", "책이름", 10000, "교수", "과목", 2, 1, "이미지URL", "게시글 내용"
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("게시글이 등록되었습니다."));

        assertThat(postRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("게시글 전체 조회")
    void getAllPosts_success() throws Exception {
        Member mockMember = createMockMember(UUID.randomUUID());
        var post = PostCreateRequest.toEntity(new PostCreateRequest(
                "제목", "책", 1234, "교수", "과목", 2, 1, "imageUrl", "내용"
        ), mockMember);

        postRepository.save(post);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].title").value("제목"));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws Exception {
        Member mockMember = createMockMember(UUID.randomUUID());
        var post = PostCreateRequest.toEntity(new PostCreateRequest(
                "제목", "책", 1234, "교수", "과목", 2, 1, "imageUrl", "내용"
        ), mockMember);

        var savedPost = postRepository.save(post);

        PostUpdateRequest updateRequest = new PostUpdateRequest("변경 제목", "수정된 내용",9999);

        mockMvc.perform(patch("/api/posts/" + savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글이 수정되었습니다."));
    }

    @Test
    @DisplayName("찜하기 성공")
    void likePost_success() throws Exception {
        UUID memberId = UUID.randomUUID();
        Member mockMember = createMockMember(memberId);

        var post = postRepository.save(PostCreateRequest.toEntity(new PostCreateRequest(
                "제목", "책", 5000, "교수", "과목", 2, 1, "img", "내용"
        ), mockMember));

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes?memberId=" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 완료되었습니다."));
    }

    @Test
    @DisplayName("찜 해제 성공")
    void unlikePost_success() throws Exception {
        UUID memberId = UUID.randomUUID();
        Member mockMember = createMockMember(memberId);

        var post = postRepository.save(PostCreateRequest.toEntity(new PostCreateRequest(
                "제목", "책", 5000, "교수", "과목", 2, 1, "img", "내용"
        ), mockMember));

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes?memberId=" + memberId));

        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes?memberId=" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 해제되었습니다."));
    }
}
