package com.example.demo.domain.chat.dto.request;

import com.example.demo.domain.report.enums.ReportReason;

import java.util.UUID;

public record ReportUserRequestDto(
        ReportReason reason
) {}
