package com.example.demo.domain.post.dto.response;

import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 상세 정보 조회 성공 응답 DTO")
public class PostDetailResponse extends RsData<PostResponse> {

    public PostDetailResponse(String code, String message, PostResponse data) {
        super(code, message, data);
    }

    public static PostDetailResponse of(String code, String message, PostResponse data) {
        return new PostDetailResponse(code, message, data);
    }
}
