package com.example.demo.domain.post.entity;
// 좋아요 Entity

import jakarta.persistence.*;
import lombok.*;
import com.example.demo.domain.member.entity.Member;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "post_id"})
})
public class PostLike {

    @Id
    @GeneratedValue
    private UUID id;
    // 로그인 기능 구현 전: UUID로 임시 식별( 리팩토링 예정)
    @Column(name = "member_id", nullable = false)
    private UUID memberId; // 임시로 UUID로 처리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
