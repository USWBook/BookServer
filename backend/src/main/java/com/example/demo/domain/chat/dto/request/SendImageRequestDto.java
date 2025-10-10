package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

//이미지 전송
@Schema(description = "이미지 전송 요청 DTO")
public record SendImageRequestDto(
        @Schema(description = "채팅방 ID", example = "907c6d4f-...")
        UUID roomId,

        @Schema(description = "전송할 이미지 파일")
        MultipartFile image
) {}
