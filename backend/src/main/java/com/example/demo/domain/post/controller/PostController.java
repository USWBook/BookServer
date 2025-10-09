package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.request.CommentCreateRequest;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostSearchCondition;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostDetailResponse;
import com.example.demo.domain.post.dto.response.PostListResponse;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.annotation.swagger.ApiUnauthorizedResponse;
import com.example.demo.global.security.userdetails.CustomUserDetails;
import com.example.demo.global.response.Empty;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성")
    @ApiSuccessResponse(
            responseCode = "201",
            description = "게시글 생성 성공",
            message = "게시글이 성공적으로 생성되었습니다.",
            dataType = UUID.class
    )
    @ApiUnauthorizedResponse
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RsData<?> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostCreateRequest request) {

        UUID postId = postService.createPost(userDetails.getId(), request);
        return RsData.of("201", "게시글이 성공적으로 등록되었습니다.", postId);
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
    @Operation(summary = "게시글 목록 동적 검색 및 페이징 조회", description = "다양한 조건으로 게시글을 검색하고 페이징하여 조회합니다.")
    @Parameters({
            @Parameter(name = "grade", description = "학년 필터", example = "2"),
            @Parameter(name = "status", description = "판매 상태 필터 (판매중, 판매완료)", example = "판매중"),
            @Parameter(name = "bookName", description = "책 이름 검색어", example = "자료구조"),
            @Parameter(name = "className", description = "강의명 검색어", example = "컴퓨터네트워크"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지당 게시물 수", example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc")
    })
//    @ApiResponse(responseCode = "200", description = "조회 성공",
//            content = @Content(schema = @Schema(implementation = PostListResponse.class)))
    @ApiUnauthorizedResponse
    @GetMapping
    public RsData<Page<PostListResponse>> searchPosts(
            @ModelAttribute PostSearchCondition condition,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostListResponse> posts = postService.searchPosts(condition, pageable);
        return RsData.of("200", "게시글 목록 조회에 성공했습니다.", posts);
    }


    @Operation(summary = "게시글 단건 조회", description = "ID로 특정 게시글의 상세 정보를 조회합니다.")
    @ApiSuccessResponse(
            description = "조회 성공",
            message = "게시글 상세 조회에 성공했습니다.",
            dataType = PostResponse.class
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "존재하지 않는 게시글",
            exampleName = "PostNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"해당 게시글을 찾을 수 없습니다.\", \"data\": null}"
    )
    @ApiUnauthorizedResponse
    @GetMapping("/{id}")
    public PostDetailResponse getPost(@PathVariable UUID id) {
        PostResponse post = postService.getPostById(id);
        return PostDetailResponse.of("200", "게시글 상세 조회에 성공했습니다.", post);
    }

    @Operation(summary = "게시글 삭제", description = "ID로 특정 게시글을 삭제합니다. (본인 또는 관리자만 가능)")
    @ApiSuccessResponse(description = "삭제 성공")
    @ApiUnauthorizedResponse
    @DeleteMapping("/{id}")
    public RsData<Empty> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return RsData.of("200", "게시글이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "게시글 수정", description = "ID로 특정 게시글의 정보를 수정합니다. (본인만 가능)")
    @ApiSuccessResponse( description = "수정 성공")
    @ApiUnauthorizedResponse
    @PatchMapping("/{id}")
    public RsData<Empty> updatePost(@PathVariable UUID id, @RequestBody @Valid PostUpdateRequest request) {
        postService.updatePost(id, request);
        return RsData.of("200", "게시글이 성공적으로 수정되었습니다.");
    }

    // 찜에선 로킹 해야함
    @Operation(summary = "게시글 찜하기", description = "특정 게시글을 찜 목록에 추가합니다.")
    @ApiSuccessResponse(description = "찜하기 성공")
    @ApiUnauthorizedResponse
    @PostMapping("/{postId}/likes")
    public RsData<Empty> likePost(
            @PathVariable UUID postId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.likePost(postId, userDetails.getId());
        return RsData.of("200", "찜 완료되었습니다.");
    }

    @Operation(summary = "게시글 찜 해제", description = "특정 게시글을 찜 목록에서 제거합니다.")
    @ApiSuccessResponse(description = "찜 해제 성공")
    @ApiUnauthorizedResponse
    @DeleteMapping("/{postId}/likes")
    public RsData<Empty> unlikePost(
            @PathVariable UUID postId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.unlikePost(postId, userDetails.getId());
        return RsData.of("200", "찜 해제되었습니다.");
    }

    @Operation(summary = "댓글 작성")
    @ApiSuccessResponse(
            responseCode = "201",
            description = "댓글 작성 성공",
            message = "댓글달기 성공했습니다.",
            dataType = PostResponse.class
    )
    @ApiUnauthorizedResponse
    @PostMapping("/{postId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetailResponse postComment(
            @PathVariable UUID postId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {

        PostResponse post = postService.createComment(postId, userDetails.getId(), request);
        return PostDetailResponse.of("201", "댓글달기 성공했습니다.", post);
    }

    @Operation(summary = "댓글 수정")
    @ApiSuccessResponse(description = "댓글 수정 성공")
    @ApiUnauthorizedResponse
    @PatchMapping("/{postId}/comment/{commentId}")
    public RsData<PostResponse> updateComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {

        PostResponse post = postService.updateComment(postId, commentId, userDetails.getId(), request);
        return RsData.of("200", "댓글 수정 성공했습니다.", post);
    }

    @Operation(summary = "댓글 삭제")
    @ApiSuccessResponse( description = "댓글 삭제 성공")
    @ApiUnauthorizedResponse
    @DeleteMapping("/{postId}/comment/{commentId}")
    public RsData<PostResponse> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        PostResponse post = postService.deleteComment(postId, commentId, userDetails.getId());
        return RsData.of("200", "댓글 삭제 성공했습니다.", post);
    }

    // 아래 세개는 동적쿼리 안넣었을때 구현 해둔거임
//    @GetMapping
//    public RsData<Page<PostListResponse>> getPosts(
//            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//
//        Page<PostListResponse> posts = postService.getAllPosts(pageable);
//
//        return RsData.of("200", "게시글 목록 조회에 성공했습니다.", posts);
//    }


//    // 책이름 검색
//    @GetMapping("/book/{bookname}")
//    public RsData<Page<PostListResponse>> getBookNamePosts(
//            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
//            @PathVariable String bookname) {
//
//        Page<PostListResponse> posts = postService.getBookPosts(pageable,bookname);
//
//        return RsData.of("200", "책이름 : " + bookname + " 게시글 목록 검색에 성공했습니다.", posts);
//    }
//
//    // 강의명 검색
//    @GetMapping("/class/{classname}")
//    public RsData<Page<PostListResponse>> getClassNamePosts(
//            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
//            @PathVariable String classname) {
//
//        Page<PostListResponse> posts = postService.getClassPosts(pageable,classname);
//
//        return RsData.of("200", "강의명 : " + classname + " 게시글 목록 검색에 성공했습니다.", posts);
//    }
}

