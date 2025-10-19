package com.example.demo.domain.admin.dto.response;

import com.example.demo.domain.report.entity.UserReport;

import java.util.UUID;

public record ReportResponse(
        String reason,
        String type,
        String reporterName,
        UUID reportedThingId

) {

    public static ReportResponse of(UserReport userReport) {
        return new ReportResponse(
                userReport.getReason().getValue(),
                userReport.getReportType().getValue(),
                userReport.getReporterName(),
                userReport.getReportThingId()
        );
    }
}
