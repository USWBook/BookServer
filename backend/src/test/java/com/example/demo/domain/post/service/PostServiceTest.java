package com.example.demo.domain.post.service;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;

import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("PostService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private UserRepository userRepository;

    private UUID postId;
    private UUID userId;
    private UUID majorId;
    private Post post;
    private User user;
    private Major major;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        userId = UUID.randomUUID();
        majorId = UUID.randomUUID();

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
    }

    @Test
    void createPost_success() {
        PostCreateRequest request = new PostCreateRequest(
                post.getTitle(),
                post.getPostName(),
                post.getPostPrice(),
                post.getProfessor(),
                post.getCourseName(),
                post.getGrade().getValue(),
                post.getSemester().getValue(),
                post.getPostImage(),
                post.getContent(),
                major.getId()
        );

        given(majorRepository.findById(major.getId())).willReturn(Optional.of(major));
        given(userRepository.findById(userId));
        given(postRepository.save(any(Post.class))).willReturn(post);

        UUID result = postService.createPost(userId,request);

        assertThat(result).isEqualTo(postId);
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 단건 조회 - 성공")
    void getPostById_success() {
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        PostResponse result = postService.getPostById(postId);

        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo(post.getTitle());
        then(postRepository).should().findById(postId);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 실패 (존재하지 않음)")
    void getPostById_fail_notFound() {
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void updatePost_success() {
        PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용", 20000);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        postService.updatePost(postId, request);

        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
        assertThat(post.getPostPrice()).isEqualTo(20000);
    }

    @Test
    @DisplayName("게시글 수정 - 실패 (존재하지 않음)")
    void updatePost_fail_notFound() {
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        PostUpdateRequest request = new PostUpdateRequest("제목", "내용", 1000);

        assertThatThrownBy(() -> postService.updatePost(postId, request))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void deletePost_success() {
        // 게시글 존재 여부를 true로 설정
        given(postRepository.existsById(postId)).willReturn(true);

        // deleteById mocking
        willDoNothing().given(postRepository).deleteById(postId);

        // when
        postService.deletePost(postId);

        // then
        then(postRepository).should().deleteById(postId);
    }

    @Test
    @DisplayName("찜하기 - 성공 (중복 아님)")
    void likePost_success() {
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.empty());
        given(postLikeRepository.save(any(PostLike.class))).willReturn(
                PostLike.builder()
                        .userId(userId)
                        .post(post)
                        .build()
        );

        postService.likePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("찜하기 - 실패 (이미 찜한 경우)")
    void likePost_alreadyLiked() {
        PostLike alreadyLiked = PostLike.builder().userId(userId).post(post).build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.of(alreadyLiked));

        postService.likePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should(never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("찜 해제 - 성공")
    void unlikePost_success() {
        PostLike like = PostLike.builder().post(post).userId(userId).build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.of(like));

        postService.unlikePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should().delete(like);
    }

    @Test
    @DisplayName("찜 해제 - 실패 (찜하지 않은 상태)")
    void unlikePost_noLikeRecord() {
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.empty());

        postService.unlikePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should(never()).delete(any());
    }
}
