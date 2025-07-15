package com.example.demo.domain.post.entity;
// 게시글 Entity

//import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.post.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.demo.domain.user.entity.Member;
import java.time.LocalDateTime;
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

    private Integer grade;

    private Integer semester;

    private String postImage;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;
    // 게시글 수정
    public void updatePost(String title, String content, Integer postPrice) {
        this.title = title;
        this.content = content;
        this.postPrice = postPrice;
        this.modifiedAt = LocalDateTime.now();
    }
    // 게시글 상태를 '판매완료'로 변경
    public void markAsSold() {
        this.status = PostStatus.판매완료;
    }
    // 좋아요 수 증가
    public void increaseLike() {
        this.likeCount++;
    }
    // 좋아요 수 감소
    public void decreaseLike() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}