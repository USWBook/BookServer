package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import com.example.demo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "게시물 생성 요청 DTO")
public record PostCreateRequest(
        @Schema(description = "게시물 제목", example = "자료구조 싸게 팝니다")
        @NotBlank String title,
        @Schema(description = "책 제목", example = "자료구조")
        @NotBlank String postName,
        @Schema(description = "가격", example = "10000")
        @NotNull Integer postPrice,
        @Schema(description = "교수명", example = "홍길동 교슈")
        String professor,
        @Schema(description = "강의명", example = "컴공 자료구조강의")
        String courseName,
        @Schema(description = "학년", example = "4")
        @NotNull Integer grade,
        @Schema(description = "학기", example = "2")
        @NotNull Integer semester,
        @Schema(description = "사진 경로", example = "https://example.com/image.jpg")
        String postImage,
        @Schema(description = "게시물 본문", example = "졸업해서 싸게 팝니다")
        String content,
        @Schema(description = "전공 ID", example = "a1b2c3d4-...")
        @NotNull UUID majorId
) {
    public static Post toEntity(PostCreateRequest request, User seller, Major major) {
        return Post.builder()
                .title(request.title())
                .postName(request.postName())
                .postPrice(request.postPrice())
                .professor(request.professor())
                .courseName(request.courseName())
                .grade(Grade.fromValue(request.grade())) // 숫자를 Enum으로 변환
                .semester(Semester.fromValue(request.semester())) // 숫자를 Enum으로 변환
                .postImage(request.postImage())
                .content(request.content())
                .seller(seller)
                .status(PostStatus.판매중)
                .major(major)
                .build();
    }
}
