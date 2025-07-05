package com.example.demo.domain.post.controller;
// 게시글 컨트롤러
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createPost(@RequestBody PostCreateRequest request) {
        UUID postId = postService.createPost(request);
        return ResponseEntity.ok().body(postId);
    }
    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }
    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.ok().body("삭제되었습니다.");
    }
    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable UUID id, @RequestBody PostUpdateRequest request) {
        postService.updatePost(id, request);
        return ResponseEntity.ok().body("게시글이 수정되었습니다.");
    }
    // 찜하기
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> likePost(@PathVariable UUID postId, @RequestParam UUID memberId) {
        postService.likePost(postId, memberId);
        return ResponseEntity.ok().body("찜 완료되었습니다.");
    }
    // 찜 해제
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> unlikePost(@PathVariable UUID postId, @RequestParam UUID memberId) {
        postService.unlikePost(postId, memberId);
        return ResponseEntity.ok().body("찜 해제되었습니다.");
    }
}