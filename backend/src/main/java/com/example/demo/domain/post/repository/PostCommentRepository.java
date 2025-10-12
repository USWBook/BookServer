package com.example.demo.domain.post.repository;

import com.example.demo.domain.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
    // 댓글 불러올때 해당 댓글이 달린 게시물도 불러옴
    @Query("SELECT pc FROM PostComment pc JOIN FETCH pc.post WHERE pc.id = :commentId")
    Optional<PostComment> findByIdWithPost(@Param("commentId") UUID commentId);
}
