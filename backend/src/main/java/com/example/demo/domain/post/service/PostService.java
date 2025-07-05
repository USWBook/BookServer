package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;
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

    // 게시글 생성
    @Transactional
    public UUID createPost(PostCreateRequest request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .postName(request.getPostName())
                .postPrice(request.getPostPrice())
                .professor(request.getProfessor())
                .courseName(request.getCourseName())
                .grade(request.getGrade())
                .semester(request.getSemester())
                .postImage(request.getPostImage())
                .content(request.getContent())
                .status(PostStatus.판매중)
                .likeCount(0)
                .build();

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
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        return PostResponse.from(post);
    }
    // 게시글 삭제
    @Transactional
    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }
    // 게시글 수정
    @Transactional
    public void updatePost(UUID postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        post.updatePost(request.getTitle(), request.getContent(), request.getPostPrice());
    }
    // 찜하기
    @Transactional
    public void likePost(UUID postId, UUID memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        boolean alreadyLiked = postLikeRepository.findByMemberIdAndPost(memberId, post).isPresent();
        if (alreadyLiked) return; // 중복 찜 방지

        postLikeRepository.save(PostLike.builder()
                .memberId(memberId)
                .post(post)
                .build());

        post.increaseLike();
    }
    // 찜 해제
    @Transactional
    public void unlikePost(UUID postId, UUID memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        postLikeRepository.findByMemberIdAndPost(memberId, post)
                .ifPresent(postLike -> {
                    postLikeRepository.delete(postLike);
                    post.decreaseLike();
                });
    }
}