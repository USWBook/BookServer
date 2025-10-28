package com.example.demo.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시물 수정 요청 DTO")
public record PostUpdateRequest(
        @Schema(description = "게시물 제목", example = "자료구조 싸게 팝니다")
        String title,
        @Schema(description = "게시물 본문", example = "졸업해서 싸게 팝니다")
        String content,
        @Schema(description = "가격", example = "10000")
        Integer postPrice,
        @Schema(description = "사진 경로", example = "https://s3.bucket/book.jpg")
        String postImage
) {
}
