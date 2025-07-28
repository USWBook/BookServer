package com.example.demo.domain.post.controller;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.repository.UserRepository;
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
    @Autowired private UserRepository userRepository;
    @Autowired private MajorRepository majorRepository;

    private User createAndSaveMockMember(UUID id) {
        Major major = majorRepository.save(Major.builder()
                .name("컴퓨터공학")
                .build());

        User user = User.builder()
                .id(id)
                .email("test@example.com")
                .password("password")
                .name("테스트유저")
                .studentId("20230001")
                .major(major)
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() throws Exception {
        User user = createAndSaveMockMember(UUID.randomUUID());
        Major major = user.getMajor();

        PostCreateRequest request = new PostCreateRequest(
                "제목", "책이름", 10000, "교수", "과목", 2, 1, "이미지URL", "게시글 내용", major.getId()
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 등록되었습니다."));

        assertThat(postRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("게시글 전체 조회")
    void getAllPosts_success() throws Exception {
        User user = createAndSaveMockMember(UUID.randomUUID());
        Major major = user.getMajor();

        PostCreateRequest request = new PostCreateRequest(
                "제목", "책", 1234, "교수", "과목", 2, 1, "imageUrl", "내용", major.getId()
        );

        Post post = PostCreateRequest.toEntity(request, user, major);
        postRepository.save(post);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].title").value("제목"));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws Exception {
        User user = createAndSaveMockMember(UUID.randomUUID());
        Major major = user.getMajor();

        PostCreateRequest request = new PostCreateRequest(
                "제목", "책", 1234, "교수", "과목", 2, 1, "imageUrl", "내용", major.getId()
        );
        Post post = PostCreateRequest.toEntity(request, user, major);
        Post savedPost = postRepository.save(post);

        PostUpdateRequest updateRequest = new PostUpdateRequest("변경 제목", "수정된 내용", 9999);

        mockMvc.perform(patch("/api/posts/" + savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("찜하기 성공")
    void likePost_success() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = createAndSaveMockMember(userId);
        Major major = user.getMajor();

        PostCreateRequest request = new PostCreateRequest(
                "제목", "책", 5000, "교수", "과목", 2, 1, "img", "내용", major.getId()
        );
        Post post = postRepository.save(PostCreateRequest.toEntity(request, user, major));

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 완료되었습니다."));
    }

    @Test
    @DisplayName("찜 해제 성공")
    void unlikePost_success() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = createAndSaveMockMember(userId);
        Major major = user.getMajor();

        PostCreateRequest request = new PostCreateRequest(
                "제목", "책", 5000, "교수", "과목", 2, 1, "img", "내용", major.getId()
        );
        Post post = postRepository.save(PostCreateRequest.toEntity(request, user, major));

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes?userId=" + userId));
        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 해제되었습니다."));
    }
}
