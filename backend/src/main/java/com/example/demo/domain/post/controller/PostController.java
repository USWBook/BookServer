package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.request.PostCreateRequest;
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

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostCreateRequest request) {
        UUID postId = postService.createPost(request);
        return ResponseEntity.ok().body(postId);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.ok().body("삭제되었습니다.");
    }
}