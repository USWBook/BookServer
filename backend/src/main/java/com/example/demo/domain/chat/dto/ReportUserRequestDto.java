package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReportUserRequestDto {
    private UUID reportedUserId;
    private UUID reporterId;
    private String reason;
}
