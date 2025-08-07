package com.example.demo.domain.major.repository;

import com.example.demo.domain.major.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MajorRepository extends JpaRepository<Major, UUID> {
    Optional<Major> findByName(String name); // 전공 이름으로 조회
    boolean existsByName(String name); // 전공 이름 중복 확인용
}

