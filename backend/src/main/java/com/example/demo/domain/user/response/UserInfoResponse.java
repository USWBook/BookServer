package com.example.demo.domain.user.response;


import com.example.demo.domain.user.entity.User;

public record UserInfoResponse(
        String name,
        String majorName,
        String email,
        Integer grade,
        Integer semester
) {

    public static UserInfoResponse from(User user) {
        String majorName = (user.getMajor() != null) ? user.getMajor().getName() : null;

        return new UserInfoResponse(
                user.getName(),
                majorName,
                user.getEmail(),
                user.getGrade(),
                user.getSemester()
        );
    }
}
