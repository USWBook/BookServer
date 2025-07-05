package com.example.demo.domain.post.dto.request;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.member.entity.Member; // 작성자 정보가 필요할 경우
import com.example.demo.domain.post.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostCreateRequest {
    private String title;
    private String postName;
    private Integer postPrice;
    private String professor;
    private String courseName;
    private Integer grade;
    private Integer semester;
    private String postImage;
    private String content;

    // toEntity 메서드 추가
    public static Post toEntity(PostCreateRequest request, Member seller) {
        return Post.builder()
                .title(request.getTitle())
                .postName(request.getPostName())
                .postPrice(request.getPostPrice())
                .professor(request.getProfessor())
                .courseName(request.getCourseName())
                .grade(request.getGrade())
                .semester(request.getSemester())
                .postImage(request.getPostImage())
                .content(request.getContent())
                .seller(seller)  //
                .status(PostStatus.판매중) // 기본값 설정 필요
                .build();
    }
}