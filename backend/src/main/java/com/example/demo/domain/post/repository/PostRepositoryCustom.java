package com.example.demo.domain.post.repository;

import com.example.demo.domain.post.dto.request.PostSearchCondition;
import com.example.demo.domain.post.dto.response.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 동적 쿼리 검색 인터페이스
public interface PostRepositoryCustom {
    Page<PostListResponse> search(PostSearchCondition condition, Pageable pageable);
}
