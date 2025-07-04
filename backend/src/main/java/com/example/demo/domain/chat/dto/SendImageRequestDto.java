package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendImageRequestDto {
    private UUID roomId;
    private UUID senderId;
    private MultipartFile image;
}
