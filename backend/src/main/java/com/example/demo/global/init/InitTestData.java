package com.example.demo.global.init;

import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class InitTestData {

    private final InitDataHelper helper;
    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PostRepository postRepository;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("🔄 [InitTestData] 테스트 데이터 초기화 시작");

        // 1. 기존 데이터 삭제
        postRepository.deleteAll();
        userRepository.deleteAll();
        majorRepository.deleteAll();

        // 2. 전공 생성
        Major csMajor = helper.createMajor("컴퓨터공학과");
        Major mechMajor = helper.createMajor("기계공학과");

        // 3. 사용자 생성
        User user = helper.createUser(
                "user@example.com",
                "password123",
                "홍길동",
                "20230001",
                csMajor,
                Role.USER,
                UserStatus.ACTIVE
        );

        User admin = helper.createUser(
                "admin@example.com",
                "admin1234",
                "관리자",
                "00000000",
                mechMajor,
                Role.ADMIN,
                UserStatus.ACTIVE
        );

        // 4. 게시글 생성
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

        log.info("✅ [InitTestData] 테스트 데이터 초기화 완료");
    }
}
