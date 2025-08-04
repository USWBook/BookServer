package com.example.demo.domain.major.controller;

import com.example.demo.domain.major.response.MajorResponse;
import com.example.demo.domain.major.service.MajorService;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/major")
public class MajorController {

    private final MajorService majorService;

    @GetMapping("/list")
    public RsData<?> getAllMajors() {
        List<MajorResponse> majors = majorService.getAllMajors();
        return new RsData<>("200", "전공 목록 조회 성공", majors);
    }
}
