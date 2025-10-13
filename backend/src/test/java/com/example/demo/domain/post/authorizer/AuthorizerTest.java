package com.example.demo.domain.post.authorizer;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.repository.PostCommentRepository;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class) // Mockito 활성화 어노테이션 추가
@DisplayName("CommentAuthorizer 단위 테스트")
class AuthorizerTest {

    @InjectMocks
    private CommentAuthorizer commentAuthorizer;

    @InjectMocks
    private PostAuthorizer postAuthorizer;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostRepository postRepository;

    private UUID postId;
    private UUID userId;
    private UUID majorId;
    private UUID commentId;

    private Post post;
    private User user;
    private Major major;
    private PostComment comment;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();
        majorId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        major = Major.builder()
                .id(majorId)
                .name("컴퓨터공학")
                .build();

        user = User.builder()
                .id(userId)
                .major(major)
                .build();

        post = Post.builder()
                .id(postId)
                .title("제목")
                .postName("책 이름")
                .postPrice(10000)
                .postImage("이미지")
                .content("내용")
                .professor("교수")
                .courseName("강의")
                .grade(Grade.GRADE_1)
                .semester(Semester.SEMESTER_1)
                .status(PostStatus.PostStatus_1)
                .likeCount(0)
                .seller(user)
                .major(major)
                .build();

        comment = PostComment.builder()
                .post(post)
                .user(user)
                .content("댓글")
                .build();
    }

    @Test
    @DisplayName("댓글 권환 확인 성공")
    void commentAuthority_sucess() {
        // given
        given(postCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        boolean hasAuthority = commentAuthorizer.hasAuthority(commentId, userId);

        // then
        assertThat(hasAuthority).isTrue();
    }

    @Test
    @DisplayName("댓글 권한 확인 실패 - 다른 사용자가 작성한 댓글")
    void commentAuthority_fail_notAuthor() {
        // given
        // 현재 로그인한 사용자 ID => userId
        UUID anotherID = UUID.randomUUID();      // 타 댓글 수정 시도 ID

        //  ReflectionTestUtils를 사용해 comment 객체의 'id' 필드에 값을 강제로 주입
        ReflectionTestUtils.setField(comment, "id", commentId);

        // postCommentRepository의 findById가 호출되면, 위에서 만든 댓글 객체를 반환하도록 설정
        given(postCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        //  commentAuthorizer의 실제 로직을 호출 (현재 사용자는 anotherID)
        boolean hasAuthority = commentAuthorizer.hasAuthority(commentId, anotherID);

        // then
        //  댓글 작성자(userId)와 현재 사용자(anotherID)가 다르므로 false가 반환되어야 함
        assertThat(hasAuthority).isFalse();
    }

    @Test
    @DisplayName("게시물 권환 확인 성공")
    void postAuthority_success() {
        // given
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        boolean hasAuthority = postAuthorizer.hasAuthority(postId, userId);

        // then
        assertThat(hasAuthority).isTrue();
    }

    @Test
    @DisplayName("게시물 권한 확인 실패 - 다른 사용자가 작성한 게시물")
    void postAuthority_fail_notAuthor() {
        // given
        UUID anotherID = UUID.randomUUID();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        boolean hasAuthority = postAuthorizer.hasAuthority(postId, anotherID);

        // then
        assertThat(hasAuthority).isFalse();
    }
}