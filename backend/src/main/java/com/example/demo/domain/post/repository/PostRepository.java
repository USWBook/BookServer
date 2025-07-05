package com.example.demo.domain.post.repository;
// 게시글 저장소
import com.example.demo.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}