package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.request.CommentCreateRequest;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostListResponse;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.domain.user.dto.CustomUserDetails;
import com.example.demo.global.response.Empty;
import com.example.demo.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public RsData<?> createPost(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody PostCreateRequest request) {
        UUID postId = postService.createPost(userDetails.getId(),request);
        return new RsData<>("201", "게시글이 성공적으로 등록되었습니다.", postId);
    }

    // 게시글 전체 조회
    /**
     기본 요청 (0페이지, 10개씩, 최신순):
     GET /api/posts

     2페이지 요청 (0부터 시작):
     GET /api/posts?page=1

     페이지당 20개씩 보기:
     GET /api/posts?size=20

     2페이지, 20개씩 보기:
     GET /api/posts?page=1&size=20

     좋아요 많은 순으로 정렬하기:
     GET /api/posts?sort=likeCount,desc

     종합 예시 (1페이지, 15개씩, 가격 낮은 순):
     GET /api/posts?page=0&size=15&sort=postPrice,asc
     */
    @GetMapping
    public RsData<Page<PostListResponse>> getPosts(
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostListResponse> posts = postService.getAllPosts(pageable);

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

    // 찜에선 로킹 해야함(아직 안함)
    // 찜하기
    @PostMapping("/{postId}/likes")
    public RsData<Empty> likePost(@PathVariable UUID postId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.likePost(postId, userDetails.getId());
        return new RsData<>("200", "찜 완료되었습니다.");
    }

    // 찜 해제
    @DeleteMapping("/{postId}/likes")
    public RsData<Empty> unlikePost(@PathVariable UUID postId,  @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.unlikePost(postId, userDetails.getId());
        return new RsData<>("200", "찜 해제되었습니다.");
    }

    // 댓글 달기
    @PostMapping("/{postId}/comment")
    public RsData<PostResponse> postComment(@PathVariable UUID postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody @Valid CommentCreateRequest request) {

        PostResponse post = postService.createComment(postId,userDetails.getId(),request);
        return new RsData<>("200", "댓글달기 성공했습니다.", post);
    }

    // 댓글 수정
    @PatchMapping("/{postId}/comment/{commentId}")
    public RsData<PostResponse> updateComment(@PathVariable UUID postId,
                                              @PathVariable UUID commentId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody @Valid CommentCreateRequest request) {

        PostResponse post = postService.updateComment(postId,commentId,userDetails.getId(),request);
        return new RsData<>("200", "댓글 수정 성공했습니다.", post);
    }

    // 댓글 삭제
    @DeleteMapping("/{postId}/comment/{commentId}")
    public RsData<PostResponse> deleteComment(@PathVariable UUID postId,
                                              @PathVariable UUID commentId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        PostResponse post = postService.deleteComment(postId,commentId,userDetails.getId());
        return new RsData<>("200", "댓글 삭제 성공했습니다.", post);
    }
}
