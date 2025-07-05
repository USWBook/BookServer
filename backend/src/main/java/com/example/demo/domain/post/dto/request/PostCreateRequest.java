package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.post.enums.PostStatus;

import java.util.UUID;

public record PostCreateRequest(
        String title,
        String postName,
        Integer postPrice,
        String professor,
        String courseName,
        Integer grade,
        Integer semester,
        String postImage,
        String content,
        UUID majorId
) {
    public static Post toEntity(PostCreateRequest request, Member seller,Major major) {
        return Post.builder()
                .title(request.title())
                .postName(request.postName())
                .postPrice(request.postPrice())
                .professor(request.professor())
                .courseName(request.courseName())
                .grade(request.grade())
                .semester(request.semester())
                .postImage(request.postImage())
                .content(request.content())
                .seller(seller)
                .status(PostStatus.판매중)
                .likeCount(0)
                .major(major)
                .build();
    }
}
