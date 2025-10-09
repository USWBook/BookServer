package com.example.demo.domain.post.dto.response;

import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "게시글 생성 성공 응답 DTO")
public class PostCreationResponse extends RsData<UUID> {
    public PostCreationResponse(String code, String message, UUID data) {
        super(code, message, data);
    }
    public static PostCreationResponse of(String code, String message, UUID data) {
        return new PostCreationResponse(code, message, data);
    }
}
