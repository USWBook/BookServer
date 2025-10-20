package com.example.demo.domain.report.entity;

import com.example.demo.domain.report.enums.ReportReason;
import com.example.demo.domain.report.enums.ReportType;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;
import jakarta.persistence.Entity;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false)
    private String reporterName;  // 신고자

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType; // 신고 대상(채팅,게시물)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;  // 신고 사유

    @Column(nullable = false)
    private UUID reportThingId; // 신고대상의 식별값

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime reportedAt;  // 신고 시간
}
