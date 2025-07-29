package com.example.demo.global.init;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.role.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev") // dev 환경에서만 실행되도록 설정 (선택)
@RequiredArgsConstructor
public class InitDevData {

    private final InitDataHelper helper;

    @PostConstruct
    public void init() {
        // 전공 생성
        Major csMajor = helper.createMajor("컴퓨터공학과");
        Major mechMajor = helper.createMajor("기계공학과");

        // 일반 사용자 생성
        User user = helper.createUser(
                "user@example.com",
                "password123",
                "홍길동",
                "20230001",
                csMajor,
                Role.USER,
                UserStatus.ACTIVE
        );

        // 관리자 계정 생성
        User admin = helper.createUser(
                "admin@example.com",
                "admin1234",
                "관리자",
                "00000000",
                mechMajor,
                Role.ADMIN,
                UserStatus.ACTIVE
        );

        // 게시글 생성
        Post post = helper.createPost(
                user,
                csMajor,
                "자료구조 책 팝니다",
                "자료구조와 알고리즘",
                "자료구조",
                "홍교수",
                2,
                1,
                "https://example.com/image.jpg",
                "상태 좋습니다. 필기 거의 없음.",
                12000
        );
    }
}
