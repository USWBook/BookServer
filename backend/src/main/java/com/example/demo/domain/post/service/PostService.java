package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
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

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }
}