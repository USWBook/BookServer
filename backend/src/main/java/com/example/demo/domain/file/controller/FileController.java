package com.example.demo.domain.file.controller;

import com.example.demo.domain.file.service.S3FileService;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.annotation.swagger.ApiUnauthorizedResponse;
import com.example.demo.global.response.RsData;
import com.example.demo.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Files", description = "단일 이미지 사전 업로드 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3FileService s3FileService;

    @Operation(summary = "이미지 사전 업로드", description = "이미지를 업로드하고 URL을 반환합니다. type=profile|post (기본: post)")
    @ApiSuccessResponse(description = "업로드 성공")
    @ApiErrorResponse(responseCode = "400", description = "파일 비어있음", exampleName = "", exampleValue = "")
    @PostMapping("/image")
    public RsData<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "post") String type
    ) {
        if (file == null || file.isEmpty()) {
            return RsData.of("400", "파일이 비어있습니다.", null);
        }

        String imageUrl = "post".equalsIgnoreCase(type)
                ? s3FileService.uploadFile(file)
                : s3FileService.uploadUserProfileImage(file);

        return RsData.of("200", "이미지 업로드 성공", imageUrl);
    }
}


