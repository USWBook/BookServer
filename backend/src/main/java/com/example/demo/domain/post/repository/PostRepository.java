package com.example.demo.domain.post.repository;
// 게시글 저장소
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    // 만약 댓글이 추가 된다면 댓글들도 페치조인 해야함
    // 만약 댓글에서 채팅을 열 수 있으려면 댓글 단사람의 식별값이 필요해서 페치조인 필요
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithCommentsAndUsers(@Param("postId") UUID postId);
}