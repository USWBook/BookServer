package com.example.demo.domain.post.repository;

// 좋아요 저장소
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByUserIdAndPost(UUID userId, Post post);
    @Query("select pl.post from PostLike pl where pl.userId = :userId")
    Page<Post> findLikedPostsByUserId(@Param("userId") UUID userId, Pageable pageable);
    void deleteByUserIdAndPost(UUID userId, Post post);
}