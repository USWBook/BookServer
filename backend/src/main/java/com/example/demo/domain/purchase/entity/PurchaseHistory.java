package com.example.demo.domain.purchase.entity;

import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "purchase_history")
public class PurchaseHistory {

    @Id
    @GeneratedValue
    @Column(name = "purchase_history_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private User buyer; // 구매자

    @OneToOne(fetch = FetchType.LAZY) // 게시글은 한 번만 판매됨
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post; // 판매된 게시글

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime transactionDate; // 거래 완료 일시

    @Builder
    public PurchaseHistory(User buyer, Post post) {
        this.buyer = buyer;
        this.post = post;
    }
}