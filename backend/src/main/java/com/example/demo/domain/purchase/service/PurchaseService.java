package com.example.demo.domain.purchase.service;

import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.exception.PostNotFoundException;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.purchase.entity.PurchaseHistory;
import com.example.demo.domain.purchase.repository.PurchaseHistoryRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @Transactional
    public void completeTransaction(UUID postId, UUID buyerId) {

        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        User buyer = userRepository.findById(buyerId).orElseThrow(UserNotFoundException::new);

        post.markAsSold();

        PurchaseHistory purchase = PurchaseHistory.builder()
                .buyer(buyer)
                .post(post)
                .build();
        purchaseHistoryRepository.save(purchase);
    }
}
