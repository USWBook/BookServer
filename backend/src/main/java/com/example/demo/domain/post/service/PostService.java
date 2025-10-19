package com.example.demo.domain.post.service;

import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.dto.request.*;
import com.example.demo.domain.post.dto.response.PostListResponse;
import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.post.exception.CommentNotFoundException;
import com.example.demo.domain.post.exception.CommentNotInPostException;
import com.example.demo.domain.post.repository.PostCommentRepository;
import com.example.demo.domain.report.entity.UserReport;
import com.example.demo.domain.report.repository.UserReportRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MajorRepository majorRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserReportRepository userReportRepository;

    // 게시글 생성
    @Transactional
    public UUID createPost(UUID userId,PostCreateRequest request) {
        Major major = majorRepository.findById(request.majorId())
                .orElseThrow(MajorNotFoundException::new);


        User seller = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Post post = PostCreateRequest.toEntity(request, seller, major);
        return postRepository.save(post).getId();
    }

    // 게시글 조회(필터링이나 검색은 PostSearchCondition에 맞게 url에 파라미터로 넘겨주면 됨)
    public Page<PostListResponse> searchPosts(PostSearchCondition condition, Pageable pageable) {
        return postRepository.search(condition, pageable);
    }
    // 아래 세개는 동적쿼리 안넣었을때 구현 해둔거임
//    @Transactional(readOnly = true)
//    public Page<PostListResponse> getAllPosts(Pageable pageable) {
//        return postRepository.findAllWithCommentCount(pageable);
//    }
    //    public Page<PostListResponse> getBookPosts(Pageable pageable, String bookname) {
//        return postRepository.findBookNameWithCommentCount(pageable,bookname);
//    }
//
//    public Page<PostListResponse> getClassPosts(Pageable pageable, String classname) {
//        return postRepository.findClassNameWithCommentCount(pageable,classname);
//    }




    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findByIdWithCommentsAndUsers(id)
                .orElseThrow(PostNotFoundException::new);
        return PostResponse.from(post);
    }

    // 게시글 삭제
    @PreAuthorize("isAuthenticated() and @postAuthorizer.hasAuthority(#postId, principal.id)")
    @Transactional
    public void deletePost(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }
        postRepository.deleteById(postId);
    }

    // 게시글 수정
    @PreAuthorize("isAuthenticated() and @postAuthorizer.hasAuthority(#postId, principal.id)")
    @Transactional
    public PostResponse updatePost(UUID postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        post.updatePost(request.title(), request.content(), request.postPrice());

        return PostResponse.from(post);
    }

    // 찜하기
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        Post post = postRepository.findByIdWithPessimisticLock(postId)
                .orElseThrow(PostNotFoundException::new);

        boolean alreadyLiked = postLikeRepository.findByUserIdAndPost(userId, post).isPresent();
        if (alreadyLiked) return; // 중복 방지

        postLikeRepository.save(PostLike.builder()
                .userId(userId)
                .post(post)
                .build());

        post.increaseLike();
    }

    // 찜 해제
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        Post post = postRepository.findByIdWithPessimisticLock(postId)
                .orElseThrow(PostNotFoundException::new);

        postLikeRepository.findByUserIdAndPost(userId, post)
                .ifPresent(postLike -> {
                    postLikeRepository.delete(postLike);
                    post.decreaseLike();
                });
    }

    // 댓글 달기
    @Transactional
    public PostResponse createComment(UUID postId, UUID userId, @Valid CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PostComment comment = new PostComment(post,user,request.content());

        post.addComment(comment);
        postCommentRepository.save(comment);

        return PostResponse.from(post);
    }

     // 댓글 수정
     @PreAuthorize("isAuthenticated() and @commentAuthorizer.hasAuthority(#commentId, principal.id)")
     @Transactional
    public PostResponse updateComment(UUID postId, UUID commentId, CommentCreateRequest request) {
         PostComment comment = postCommentRepository.findByIdWithPost(commentId)
                 .orElseThrow(CommentNotFoundException::new);

         if (!comment.getPost().getId().equals(postId)) {
                throw new CommentNotInPostException();
         }

         comment.updateContent(request.content());

         return PostResponse.from(comment.getPost());
    }

    // 댓글 삭제
    @PreAuthorize("isAuthenticated() and @commentAuthorizer.hasAuthority(#commentId, principal.id)")
    @Transactional
    public PostResponse deleteComment(UUID postId, UUID commentId) {
        PostComment comment = postCommentRepository.findByIdWithPost(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (!comment.getPost().getId().equals(postId)) {
            throw new CommentNotInPostException();
        }

        postCommentRepository.delete(comment);

        return PostResponse.from(comment.getPost());
    }

    // 판매중으로 변경
    @PreAuthorize("isAuthenticated() and @postAuthorizer.hasAuthority(#postId, principal.id)")
    @Transactional
    public void sellPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        post.markAsSell();

        return;
    }

    // 판매완료로 변경
    @PreAuthorize("isAuthenticated() and @postAuthorizer.hasAuthority(#postId, userId)")
    @Transactional
    public void soldPost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        post.markAsSold();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePostByAdmin(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        postRepository.delete(post);
    }

    @Transactional
    public void reportPost(PostReportRequest request, UUID id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        UserReport report = PostReportRequest.toUserReport(request,user.getName());

        userReportRepository.save(report);
    }
}
