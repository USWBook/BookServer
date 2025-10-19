package com.example.demo.domain.report.service;

import com.example.demo.domain.admin.dto.response.ReportResponse;
import com.example.demo.domain.report.entity.UserReport;
import com.example.demo.domain.report.repository.UserReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserReportRepository userReportRepository;

    public List<ReportResponse> reportList() {
        List<UserReport> userReports = userReportRepository.findAll();

        return userReports.stream()
                .map(ReportResponse::of)
                .collect(Collectors.toList());
    }
}
