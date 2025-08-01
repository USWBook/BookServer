package com.example.demo.global.init;

import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatRoomService;
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
    private final ChatRoomService chatRoomService;

    @PostConstruct
    public void init() {
        if (helper.countUsers() > 0) {
            log.info("✅ 기존 유저가 존재하여 초기화 생략");
            return;
        }

        log.info("📌 InitDevData 시작");
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
        //채팅확인을 위해 유저 추가
        User user2 = helper.createUser(
                "user2@example.com",
                "password123",
                "김철수",
                "20230002",
                csMajor,
                Role.USER,
                UserStatus.ACTIVE
        );
        User user3 = helper.createUser(
                "user3@example.com",
                "password123",
                "이영희",
                "20230003",
                mechMajor,
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

        // 채팅방 생성 (이미 있으면 조회)
        ChatRoom chatRoom = helper.createChatRoom(
                post.getId(),
                post.getSeller().getId(),
                user2.getId()
        );

        // 샘플 채팅 메시지 생성
        ChatMessage chatMessage = helper.createChatMessage(
                chatRoom.getRoomId(),
                user2.getId(),
                "안녕하세요! 이 책 아직 판매 중인가요?"
        );

        log.info("✅ 개발용 초기 데이터 및 채팅방 생성 완료");
    }
}
