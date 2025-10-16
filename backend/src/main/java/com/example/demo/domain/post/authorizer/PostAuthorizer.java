package com.example.demo.domain.post.authorizer;

import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("postAuthorizer")
@RequiredArgsConstructor
public class PostAuthorizer {

    private final PostRepository postRepository;

    // 인가 로직 메서드
    public boolean hasAuthority(UUID postId, UUID userID) {
        if(userID == null) return false;
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);


        return post.getSeller().getId().equals(userID);
    }
}
