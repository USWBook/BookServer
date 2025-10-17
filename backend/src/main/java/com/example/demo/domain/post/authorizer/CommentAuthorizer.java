package com.example.demo.domain.post.authorizer;

import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.post.exception.CommentNotFoundException;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostCommentRepository;
import com.example.demo.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("commentAuthorizer")
@RequiredArgsConstructor
public class CommentAuthorizer {

    private final PostCommentRepository commentRepository;

    // 인가 로직 메서드
    public boolean hasAuthority(UUID commentId, UUID userID) {
        if(userID == null) return false;
        PostComment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);


        return comment.getUser().getId().equals(userID);
    }
}
