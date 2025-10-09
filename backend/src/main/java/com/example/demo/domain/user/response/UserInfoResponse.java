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
        Integer semester
) {
    public static UserInfoResponse from(User user) {
//        // 사용자가 null이거나 탈퇴 상태일 경우처리
//        if (user == null || user.getStatus() == UserStatus.WITHDRAWN) {
//            return new UserInfoResponse("탈퇴한 사용자", null, null, null, null);
//        }

        String majorName = (user.getMajor() != null) ? user.getMajor().getName() : null;

        // enum에서 숫자 값을 추출하여 DTO에 담음.
        Integer gradeValue = (user.getGrade() != null) ? user.getGrade().getValue() : null;
        Integer semesterValue = (user.getSemester() != null) ? user.getSemester().getValue() : null;

        return new UserInfoResponse(
                user.getName(),
                majorName,
                user.getEmail(),
                gradeValue,
                semesterValue
        );
    }
}