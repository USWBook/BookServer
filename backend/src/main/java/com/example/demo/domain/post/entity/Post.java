package com.example.demo.domain.post.entity;
// 게시글 Entity

//import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String postName;

    @Column(nullable = false)
    private Integer postPrice;

    private String professor;

    private String courseName;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    private String postImage;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    //  하나의 게시글은 여러 댓글을 가질 수 있음. (주인이 아님)
    // - cascade: 게시글이 삭제되면 댓글도 함께 삭제
    // - orphanRemoval: 컬렉션에서 댓글이 제거되면 DB에서도 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    // 게시글 수정
    public void updatePost(String title, String content, Integer postPrice) {
        if(title != null) this.title = title;
        if(content != null) this.content = content;
        if(postPrice != null) this.postPrice = postPrice;
        this.modifiedAt = LocalDateTime.now();
    }
    // 게시글 상태를 '판매완료'로 변경
    public void markAsSold() {
        this.status = PostStatus.PostStatus_2;
    }
    // 게시글 상태를 '판매중'으로 변경
    public void markAsSell() {
        this.status = PostStatus.PostStatus_1;
    }
    // 좋아요 수 증가
    public void increaseLike() {
        this.likeCount++;
    }
    // 좋아요 수 감소
    public void decreaseLike() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void addComment(PostComment comment) {
        this.comments.add(comment);
    }

}