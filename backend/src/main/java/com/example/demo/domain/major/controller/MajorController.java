package com.example.demo.domain.major.controller;

import com.example.demo.domain.major.response.MajorResponse;
import com.example.demo.domain.major.service.MajorService;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Majors", description = "전공관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/major")
public class MajorController {

    private final MajorService majorService;

    @Operation(summary = "전공 리스트 보기", description = "모든 전공 이름들을 불러옵니다")
    @ApiResponse(responseCode = "200", description = "전공리스트 불러오기 성공")
    @GetMapping("/list")
    public RsData<?> getAllMajors() {
        List<MajorResponse> majors = majorService.getAllMajors();
        return new RsData<>("200", "전공 목록 조회 성공", majors);
    }
}
