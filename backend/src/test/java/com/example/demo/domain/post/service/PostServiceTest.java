package com.example.demo.domain.post.service;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.dto.request.CommentCreateRequest;
import com.example.demo.domain.post.dto.request.PostCreateRequest;
import com.example.demo.domain.post.dto.request.PostUpdateRequest;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.post.entity.PostLike;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.post.exception.CommentNotFoundException;
import com.example.demo.domain.post.exception.CommentNotInPostException;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostCommentRepository;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;

import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private PostCommentRepository postCommentRepository;

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
                .status(PostStatus.SELLING)
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
    @DisplayName("게시글 생성 - 성공")
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
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(post);

        UUID result = postService.createPost(userId,request);

        assertThat(result).isEqualTo(postId);
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 단건 조회 - 성공")
    void getPostById_success() {
        given(postRepository.findByIdWithCommentsAndUsers(postId)).willReturn(Optional.of(post));

        PostResponse result = postService.getPostById(postId);

        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo(post.getTitle());

        then(postRepository).should().findByIdWithCommentsAndUsers(postId);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 실패 (존재하지 않음)")
    void getPostById_fail_notFound() {
        given(postRepository.findByIdWithCommentsAndUsers(postId)).willReturn(Optional.empty());

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
        given(postRepository.findByIdWithPessimisticLock(postId)).willReturn(Optional.of(post));
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

        given(postRepository.findByIdWithPessimisticLock(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.of(alreadyLiked));

        postService.likePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should(never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("찜 해제 - 성공")
    void unlikePost_success() {
        PostLike like = PostLike.builder().post(post).userId(userId).build();

        given(postRepository.findByIdWithPessimisticLock(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.of(like));

        postService.unlikePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should().delete(like);
    }

    @Test
    @DisplayName("찜 해제 - 실패 (찜하지 않은 상태)")
    void unlikePost_noLikeRecord() {
        given(postRepository.findByIdWithPessimisticLock(postId)).willReturn(Optional.of(post));
        given(postLikeRepository.findByUserIdAndPost(userId, post)).willReturn(Optional.empty());

        postService.unlikePost(postId, userId);

        assertThat(post.getLikeCount()).isEqualTo(0);
        then(postLikeRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("댓글달기 성공")
    public void createComment_success() {
        CommentCreateRequest request = CommentCreateRequest
                .builder()
                .content(comment.getContent())
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        postService.createComment(postId, userId, request);

        assertThat(request.content()).isEqualTo(post.getComments().getFirst().getContent());

    }

    @Test
    @DisplayName("댓글달기 실패 - 게시물을  찾을 수 없음(틀린 게시물 ID)")
    public void createComment_fail_notFound() {
        CommentCreateRequest request = CommentCreateRequest
                .builder()
                .content(comment.getContent())
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createComment(postId,userId,request))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("댓글 수정 성공")
    public void updateComment_success() {
        // given
        // 수정 대상이 될 원본 댓글과 ID를 준비.
        String originalContent = "이전 댓글 내용";
        PostComment existingComment =  PostComment.builder()
                .post(post)
                .user(user)
                .content(originalContent)
                .build();

        //  수정할 내용을 담은 요청 DTO를 준비합니다.
        String updatedContent = "새로운 댓글 내용";
        CommentCreateRequest request = new CommentCreateRequest(updatedContent);

        //  서비스가 commentId로 댓글을 찾아올 때, 위에서 만든 원본 댓글을 반환하도록 설정
        given(postCommentRepository.findByIdWithPost(commentId)).willReturn(Optional.of(existingComment));

        // when
        //  올바른 파라미터(postId, commentId)로 서비스 메서드를 호출
        PostResponse postResponse = postService.updateComment(postId, commentId, request);

        // then
        // 원본 댓글 객체의 내용이 요청받은 내용으로 변경되었는지 확인
        assertThat(existingComment.getContent()).isEqualTo(updatedContent);
        // 반환된 PostResponse가 null이 아닌지, ID가 올바른지 등을 확인.
        assertThat(postResponse).isNotNull();
        assertThat(postResponse.id()).isEqualTo(postId);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글 ID")
    public void updateComment_fail_commentNotFound() {
        // given
        UUID nonExistentCommentId = UUID.randomUUID(); // 존재하지 않는 댓글 ID
        CommentCreateRequest request = new CommentCreateRequest("아무 내용");

        // 서비스가 존재하지 않는 ID로 댓글을 찾으려고 할 때, 빈 Optional을 반환하도록 설정
        given(postCommentRepository.findByIdWithPost(nonExistentCommentId)).willReturn(Optional.empty());

        // when & then
        // 해당 서비스 호출 시 CommentNotFoundException이 발생하는지 검증
        assertThatThrownBy(() -> postService.updateComment(postId, nonExistentCommentId, request))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글이 다른 게시글에 속함")
    public void updateComment_fail_commentNotInPost() {
        // given
        //  다른 게시글을 하나 만듬
        UUID anotherPostId = UUID.randomUUID();
        Post anotherPost = Post.builder().id(anotherPostId).build();

        // 수정하려는 댓글은 '다른 게시글'에 속해있다고 설정
        PostComment commentOnAnotherPost = new PostComment(anotherPost, user, "다른 글의 댓글");
        CommentCreateRequest request = new CommentCreateRequest("수정 시도 내용");

        // 서비스가 commentId로 댓글을 찾으면, '다른 댓글이 달린 글'을 반환하도록 설정
        given(postCommentRepository.findByIdWithPost(commentId)).willReturn(Optional.of(commentOnAnotherPost));

        // when & then
        // '원래 게시글 ID (postId)'로 수정을 시도하지만, 댓글은 '다른 게시글'에 속해 있으므로
        // CommentNotInPostException이 발생하는지 검증
        assertThatThrownBy(() -> postService.updateComment(postId, commentId, request))
                .isInstanceOf(CommentNotInPostException.class);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    public void deleteComment_success() {
        // given
        // comment 객체는 post에 속해있다고 설정
        ReflectionTestUtils.setField(comment, "id", commentId);
        // 서비스 로직에서 comment.getPost()를 호출하므로, comment가 post를 알고 있어야 함
        // setUp()에서 이미 comment = PostComment.builder().post(post)... 로 설정되어 있으므로 OK.

        // 서비스가 commentId로 댓글을 찾아올 때, 위에서 만든 comment를 반환하도록 설정
        given(postCommentRepository.findByIdWithPost(commentId)).willReturn(Optional.of(comment));
        // delete 메서드는 void를 반환하므로 별도의 given 설정은 필요 없음

        // when
        postService.deleteComment(postId, commentId);

        // then
        // AssertJ의 상태 검증 대신, Mockito의 '행위 검증'을 사용.
        // "postCommentRepository의 delete 메서드가 'comment' 객체를 인자로 받아 정확히 1번 호출되었는가?"
        then(postCommentRepository).should().delete(comment);
    }

}
