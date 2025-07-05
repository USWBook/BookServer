package com.example.demo.domain.post.service;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.exception.MemberNotFoundException;
import com.example.demo.domain.member.repository.MemberRepository;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.exception.PostNotFoundException;
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
    private final MajorRepository majorRepository;
    private final MemberRepository memberRepository;

    // 게시글 생성
    @Transactional
    public UUID createPost(PostCreateRequest request) {
        Major major = majorRepository.findById(request.majorId())
                .orElseThrow(MajorNotFoundException::new);

        // seller: 로그인 구현 전까지 임의의 member 할당 또는 예외 처리
        Member seller = memberRepository.findAll().stream().findFirst()
                .orElseThrow(MemberNotFoundException::new);

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
        Post post = postRepository.findById(id)
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
    public void likePost(UUID postId, UUID memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        boolean alreadyLiked = postLikeRepository.findByMemberIdAndPost(memberId, post).isPresent();
        if (alreadyLiked) return; // 중복 방지

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
                .orElseThrow(PostNotFoundException::new);

        postLikeRepository.findByMemberIdAndPost(memberId, post)
                .ifPresent(postLike -> {
                    postLikeRepository.delete(postLike);
                    post.decreaseLike();
                });
    }
}
