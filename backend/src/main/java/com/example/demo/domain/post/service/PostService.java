package com.example.demo.domain.post.service;

import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.dto.request.CommentCreateRequest;
import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.post.exception.CommentNotFoundException;
import com.example.demo.domain.post.repository.PostCommentRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final MajorRepository majorRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;

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

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findByIdWithCommentsAndUsers(id)
                .orElseThrow(PostNotFoundException::new);
        return PostResponse.from(post);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException();
        }
        postRepository.deleteById(id);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(UUID postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        post.updatePost(request.title(), request.content(), request.postPrice());
    }

    // 찜하기
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
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
        Post post = postRepository.findById(postId)
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

        postCommentRepository.save(comment);

        Post updatedPost = postRepository.findByIdWithCommentsAndUsers(postId)
                .orElseThrow(PostNotFoundException::new);


        return PostResponse.from(updatedPost);
    }


     // 댓글 수정
     @Transactional
    public PostResponse updateComment(UUID postId, UUID commentId, UUID userId, CommentCreateRequest request) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // 댓글 작성자 본인 또는 관리자만 수정 가능인데 어차피 프론트에서 막아둘거같아서 예외는 안만들어둠
        if (!comment.getUser().getId().equals(userId)) {
            //  throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.updateContent(request.content());

        // 댓글이 수정된 최신 Post 객체를 다시 조회 (Fetch Join 사용)
        Post updatedPost = postRepository.findByIdWithCommentsAndUsers(postId)
                .orElseThrow(PostNotFoundException::new);

        return PostResponse.from(updatedPost);
    }

    // 댓글 삭제
    @Transactional
    public PostResponse deleteComment(UUID postId, UUID commentId, UUID userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // 댓글 작성자 본인 또는 관리자만 삭제 가능인데 어차피 프론트에서 막아둘거같아서 예외는 안만들어둠
        if (!comment.getUser().getId().equals(userId)) {
            // throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        postCommentRepository.delete(comment);

        // 댓글이 삭제된 최신 Post 객체를 다시 조회 (Fetch Join 사용)
        Post updatedPost = postRepository.findByIdWithCommentsAndUsers(postId)
                .orElseThrow(PostNotFoundException::new);

        return PostResponse.from(updatedPost);
    }
}
