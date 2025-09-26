package com.example.demo.domain.post.controller;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.init.InitTestData;
import com.example.demo.global.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
    @Autowired private InitTestData initTestData;
    @Autowired private JwtProvider jwtProvider;

    // 🔥 RedisTemplate<Object> mocking해서 Redis 의존성 제거
    @MockBean(name = "chatRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private User testUser;
    private Major testMajor;
    private String accessToken;

    @BeforeEach
    void setup() {
        // InitTestData에 의해 미리 생성된 사용자와 전공 가져오기
        testUser = postRepository.findAll().get(0).getSeller(); // 게시글 작성자
        testMajor = testUser.getMajor();

        System.out.println("✅ 토큰 사용자 이메일: " + testUser.getEmail());
        System.out.println("✅ 토큰 사용자 ROLE: " + testUser.getRole());

        accessToken = jwtProvider.generateAccessToken(testUser.getId(),testUser.getEmail(), testUser.getRole());
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "제목", "책이름", 10000, "교수", "과목", 2, 1,
                "이미지URL", "게시글 내용", testMajor.getId()
        );

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 등록되었습니다."));

        assertThat(postRepository.findAll()).hasSize(2); // 기존 1개 + 생성 1개
    }

    @Test
    @DisplayName("게시글 전체 조회")
    void getAllPosts_success() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].title").value("자료구조 책 팝니다")); // InitTestData 게시글
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws Exception {
        Post post = postRepository.findAll().get(0); // InitTestData 게시글

        PostUpdateRequest updateRequest = new PostUpdateRequest("변경 제목", "수정된 내용", 9999);

        mockMvc.perform(patch("/api/posts/" + post.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("찜하기 성공")
    void likePost_success() throws Exception {
        Post post = postRepository.findAll().get(0);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                        .param("userId", testUser.getId().toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 완료되었습니다."));
    }

    @Test
    @DisplayName("찜 해제 성공")
    void unlikePost_success() throws Exception {
        Post post = postRepository.findAll().get(0);

        // 찜 먼저 하기
        mockMvc.perform(post("/api/posts/" + post.getId() + "/likes")
                .param("userId", testUser.getId().toString())
                .header("Authorization", "Bearer " + accessToken));

        // 찜 해제
        mockMvc.perform(delete("/api/posts/" + post.getId() + "/likes")
                        .param("userId", testUser.getId().toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 해제되었습니다."));
    }
}
