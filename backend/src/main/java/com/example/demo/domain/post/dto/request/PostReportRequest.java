package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.report.entity.UserReport;
import com.example.demo.domain.report.enums.ReportReason;
import com.example.demo.domain.report.enums.ReportType;

import java.util.UUID;

public record PostReportRequest(
        ReportType type, // 채팅인지 게시물인지 구분
        ReportReason reason, // 신고사유
        UUID Id // 신고대상 식별값(예 게시물 식별값)
) {

    public static UserReport toUserReport(PostReportRequest request, String userName) {
        return UserReport.builder()
                .reporterName(userName)
                .reportType(request.type())
                .reason(request.reason())
                .reportThingId(request.Id)
                .build();
    }
}
