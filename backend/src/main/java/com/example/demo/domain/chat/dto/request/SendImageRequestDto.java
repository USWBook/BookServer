package com.example.demo.domain.chat.dto.request;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

//이미지 전송
public record SendImageRequestDto(
        UUID roomId,
        UUID senderId,
        MultipartFile image
) {}
