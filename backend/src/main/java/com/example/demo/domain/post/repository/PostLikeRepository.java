package com.example.demo.domain.post.repository;

// 좋아요 저장소
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByUserIdAndPost(UUID memberId, Post post);
    void deleteByMemberIdAndPost(UUID memberId, Post post);
}