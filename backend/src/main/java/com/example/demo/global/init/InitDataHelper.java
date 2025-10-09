package com.example.demo.global.init;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.demo.domain.chat.dto.request.CreateChatRoomRequestDto;
import com.example.demo.domain.chat.dto.request.SendMessageRequestDto;
import com.example.demo.domain.chat.entity.ChatRoom;
import com.example.demo.domain.chat.entity.ChatMessage;
import com.example.demo.domain.chat.service.ChatRoomService;
import com.example.demo.domain.chat.service.ChatService;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InitDataHelper {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChatRoomService chatRoomService;  // 채팅방 관리 서비스
    private final ChatService chatService;          // 채팅 메시지 관리 서비스

    // 사용자 생성
    public User createUser(String email, String rawPassword, String name, String studentId, Major major, Role role, UserStatus status, int grade,int semester) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .name(name)
                        .studentId(studentId)
                        .major(major)
                        .role(role)
                        .status(status)
                        .grade(Grade.fromValue(grade))
                        .semester(Semester.fromValue(semester))
                        .build()
        );
    }
    public long countUsers() {
        return userRepository.count();
    }

    // 전공 생성
    public Major createMajor(String name) {
        return majorRepository.save(
                Major.builder()
                        .name(name)
                        .build()
        );
    }

    // 게시글 생성
    public Post createPost(
            User seller,
            Major major,
            String title,
            String postName,
            String courseName,
            String professor,
            int grade,
            int semester,
            String postImage,
            String content,
            int price
    ) {
        return postRepository.save(
                Post.builder()
                        .seller(seller)
                        .major(major)
                        .title(title)
                        .postName(postName)
                        .courseName(courseName)
                        .professor(professor)
                        .grade(Grade.fromValue(grade))
                        .semester(Semester.fromValue(semester))
                        .postImage(postImage)
                        .content(content)
                        .postPrice(price)
                        .likeCount(0)
                        .status(PostStatus.판매중)
                        .build()
        );
    }
    /**
     * 채팅방 생성 (이미 있으면 기존 방 반환)
     */
    public ChatRoom createChatRoom(UUID postId, UUID buyerId) {
        // sellerId는 바깥에서 별도로 필요 없음, postId로 서버가 찾음
        CreateChatRoomRequestDto request = new CreateChatRoomRequestDto(postId);
        return chatRoomService.createOrGetRoom(request, buyerId);
    }


    /**
     * 채팅 메시지 생성(전송)
     * 기본적으로 신규 메시지이고, receiverId는 null로 둠 (필요 시 변경)
     */
    public ChatMessage createChatMessage(UUID roomId, String message, UUID senderId) {
        SendMessageRequestDto request = new SendMessageRequestDto(
                roomId,
                message
        );
        return chatService.sendChatMessage(request, senderId);
    }

}
