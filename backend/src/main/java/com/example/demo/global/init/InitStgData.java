package com.example.demo.global.init;

import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.role.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("stg") // STG 환경에서만 실행
@RequiredArgsConstructor
public class InitStgData {

    private final InitDataHelper helper;
    private final ChatRoomService chatRoomService;

    @PostConstruct
    public void init() {
        if (helper.countUsers() > 0) {
            log.info("✅ [InitStgData] 기존 유저가 존재하여 STG 초기화 스킵");
            return;
        }

        log.info("📌 [InitStgData] STG 초기 데이터 시드 시작");

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
                UserStatus.ACTIVE,
                1,
                1
        );

        // 채팅 확인용 유저
        User user2 = helper.createUser(
                "user2@example.com",
                "password123",
                "김철수",
                "20230002",
                csMajor,
                Role.USER,
                UserStatus.ACTIVE,
                2,
                2
        );
        User user3 = helper.createUser(
                "user3@example.com",
                "password123",
                "이영희",
                "20230003",
                mechMajor,
                Role.USER,
                UserStatus.ACTIVE,
                3,
                1
        );

        // 관리자 계정
        User admin = helper.createUser(
                "admin@example.com",
                "admin1234",
                "관리자",
                "00000000",
                mechMajor,
                Role.ADMIN,
                UserStatus.ACTIVE,
                4,
                2
        );

        // 게시글 샘플
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

        // 채팅방/메시지 샘플
        ChatRoom chatRoom = helper.createChatRoom(
                post.getId(),
                user2.getId()
        );

        ChatMessage chatMessage = helper.createChatMessage(
                chatRoom.getRoomId(),
                "안녕하세요! 이 책 아직 판매 중인가요?",
                user2.getId()
        );

        log.info("✅ [InitStgData] STG 초기 데이터 시드 완료");
    }
}
