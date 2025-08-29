package com.example.demo.domain.report.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.domain.report.entity.UserReport;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, UUID> {
}