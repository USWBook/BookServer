package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.global.exception.BookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private UUID postId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postId = UUID.randomUUID();
        memberId = UUID.randomUUID();
    }

    @Test
    void 게시글_생성_성공() {
        PostCreateRequest request = new PostCreateRequest(
                "제목", "물품이름", 10000, "교수", "과목", 2, 1, "url", "게시글 내용"
        );
        UUID newId = UUID.randomUUID();

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(newId);
            return post;
        });

        UUID result = postService.createPost(request);

        assertThat(result).isEqualTo(newId);
    }

    @Test
    void 게시글_단건조회_실패() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(BookException.class)
                .hasMessageContaining("존재하지 않는 게시글입니다.");
    }

    @Test
    void 게시글_삭제_성공() {
        Post post = new Post();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(postId);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void 게시글_수정_성공() {
        Post post = new Post();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest("수정된 제목", 20000, "수정된 내용");
        postService.updatePost(postId, request);

        assertThat(post.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    void 찜하기_성공() {
        Post post = new Post();
        post.setId(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.likePost(postId, memberId);

        assertThat(post.getLikeMembers()).contains(memberId);
    }

    @Test
    void 찜_해제_성공() {
        Post post = new Post();
        post.setId(postId);
        post.getLikeMembers().add(memberId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.unlikePost(postId, memberId);

        assertThat(post.getLikeMembers()).doesNotContain(memberId);
    }
}
