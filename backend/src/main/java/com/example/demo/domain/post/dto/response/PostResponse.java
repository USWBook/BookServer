package com.example.demo.domain.post.dto.response;
// 게시글 응답 DTO
import com.example.demo.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PostResponse {
    private UUID id;
    private String title;
    private String postName;
    private Integer postPrice;
    private String postImage;
    private String content;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .postName(post.getPostName())
                .postPrice(post.getPostPrice())
                .postImage(post.getPostImage())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .build();
    }
}