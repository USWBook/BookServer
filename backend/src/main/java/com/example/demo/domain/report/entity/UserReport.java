package com.example.demo.domain.report.entity;

import com.example.demo.domain.report.enums.ReportReason;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;
import jakarta.persistence.Entity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
@Entity
public class UserReport {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User reporter;  // 신고자

    @ManyToOne(fetch = FetchType.LAZY)
    private User reported;  // 피신고자

    private ReportReason reason;  // 신고 사유

    private LocalDateTime reportedAt;  // 신고 시간
}
