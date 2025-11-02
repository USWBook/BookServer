package com.example.demo.domain.user.response;


import com.example.demo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답 DTO")
public record UserInfoResponse(
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "전공 이름", example = "컴퓨터공학과")
        String majorName,

        @Schema(description = "이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "학년", example = "2")
        Integer grade,

        @Schema(description = "학기", example = "1")
        Integer semester,

        @Schema(description = "프로필 이미지 URL (nullable)", example = "https://s3.ap-northeast-2.amazonaws.com/bucket/users/abc.jpg")
        String profileImageUrl
) {
    public static UserInfoResponse from(User user, String presignedUrl) {
        String majorName = (user.getMajor() != null) ? user.getMajor().getName() : null;
        Integer gradeValue = (user.getGrade() != null) ? user.getGrade().getValue() : null;
        Integer semesterValue = (user.getSemester() != null) ? user.getSemester().getValue() : null;

        return new UserInfoResponse(
                user.getName(),
                majorName,
                user.getEmail(),
                gradeValue,
                semesterValue,
                presignedUrl
        );
    }
}