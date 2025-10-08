package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import com.example.demo.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PostCreateRequest(
        @NotBlank String title,
        @NotBlank String postName,
        @NotNull Integer postPrice,
        String professor,
        String courseName,
        @NotNull Integer grade,
        @NotNull Integer semester,
        String postImage,
        String content,
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
