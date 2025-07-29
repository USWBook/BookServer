package com.example.demo.global.init;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDataHelper {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 생성
    public User createUser(String email, String rawPassword, String name, String studentId, Major major, Role role, UserStatus status) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .name(name)
                        .studentId(studentId)
                        .major(major)
                        .role(role)
                        .status(status)
                        .build()
        );
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
                        .grade(grade)
                        .semester(semester)
                        .postImage(postImage)
                        .content(content)
                        .postPrice(price)
                        .likeCount(0)
                        .status(PostStatus.판매중)
                        .build()
        );
    }
}
