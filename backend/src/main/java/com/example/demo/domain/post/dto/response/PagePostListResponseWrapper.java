package com.example.demo.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

@Schema(name = "PagePostListResponseWrapper", description = "Page<PostListResponse> Swagger 지원용 래퍼")
public class PagePostListResponseWrapper {

    @Schema(description = "총 게시글 수", example = "123")
    public long totalElements = 123;

    @Schema(description = "총 페이지 수", example = "13")
    public int totalPages = 13;

    @Schema(description = "현재 페이지 번호", example = "0")
    public int number = 0;

    @Schema(description = "페이지 크기", example = "10")
    public int size = 10;

    @Schema(description = "게시글 목록 데이터")
    public List<PostListResponse> content;
}
