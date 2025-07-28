package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.global.response.Empty;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public RsData<?> createPost(@RequestBody PostCreateRequest request) {
        UUID postId = postService.createPost(request);
        return new RsData<>("201", "게시글이 성공적으로 등록되었습니다.", postId);
    }

    // 게시글 전체 조회
    @GetMapping
    public RsData<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return new RsData<>("200", "게시글 목록 조회에 성공했습니다.", posts);
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public RsData<PostResponse> getPost(@PathVariable UUID id) {
        PostResponse post = postService.getPostById(id);
        return new RsData<>("200", "게시글 상세 조회에 성공했습니다.", post);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public RsData<Empty> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return new RsData<>("200", "게시글이 성공적으로 삭제되었습니다.");
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public RsData<Empty> updatePost(@PathVariable UUID id, @RequestBody PostUpdateRequest request) {
        postService.updatePost(id, request);
        return new RsData<>("200", "게시글이 성공적으로 수정되었습니다.");
    }

    // 찜하기
    @PostMapping("/{postId}/likes")
    public RsData<Empty> likePost(@PathVariable UUID postId, @RequestParam UUID userId) {
        postService.likePost(postId, userId);
        return new RsData<>("200", "찜 완료되었습니다.");
    }

    // 찜 해제
    @DeleteMapping("/{postId}/likes")
    public RsData<Empty> unlikePost(@PathVariable UUID postId, @RequestParam UUID userId) {
        postService.unlikePost(postId, userId);
        return new RsData<>("200", "찜 해제되었습니다.");
    }
}
