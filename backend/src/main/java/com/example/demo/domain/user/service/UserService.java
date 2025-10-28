package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;
import com.example.demo.domain.purchase.entity.PurchaseHistory;
import com.example.demo.domain.purchase.repository.PurchaseHistoryRepository;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.dto.UploadPost;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.exception.PasswordNotEqualException;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);


        return new UserInfoResponse(
                user.getName(),
                user.getMajor().getName(),
                user.getEmail(),
                user.getGrade().getValue(),
                user.getSemester().getValue()
        );
    }

    @Transactional
    public UserInfoResponse changeInformation(UUID userId, ChangeInfoRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(MemberNotFoundException::new);

        Major newMajor = findMajorOrNull(request.majorId());

//        Grade newGrade = (request.grade() != null) ? Grade.fromValue(request.grade()) : null;
//        Semester newSemester = (request.semester() != null) ? Semester.fromValue(request.semester()) : null;
        
        currentUser.updateProfile(request.name(), newMajor, request.grade(), request.semester());

        return UserInfoResponse.from(currentUser);
    }

    private Major findMajorOrNull(UUID majorId) {
        if (majorId == null) {
            return null;
        }
        return majorRepository.findById(majorId)
                .orElseThrow(MajorNotFoundException::new);
    }


    @Transactional
    public void withdraw(UUID userId, String password) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if(!user.getPassword().equals(password)) throw new PasswordNotEqualException();

        user.withdraw();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void banUser(String sellerName) {
        User user = userRepository.findByName(sellerName).orElseThrow(UserNotFoundException::new);
        user.ban();
    }

    @Transactional(readOnly = true)
    public Page<UploadPost> getMyPosts(UUID userId, Pageable pageable) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }
        Page<Post> postPage = postRepository.findBySellerId(userId, pageable);

        return postPage.map(UploadPost::from);
    }

    @Transactional(readOnly = true)
    public Page<UploadPost> getMyLikePosts(UUID userId, Pageable pageable) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        Page<Post> postPage = postLikeRepository.findLikedPostsByUserId(userId, pageable);

        return postPage.map(UploadPost::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPurchaseList(UUID currentUserId,Pageable pageable) {

        Page<PurchaseHistory> purchases = purchaseHistoryRepository.findByBuyerId(currentUserId, pageable);

        return purchases
                .map(ph ->
                        PostResponse.from(ph.getPost()));

//        return purchases.map(PurchaseHistory::getPost) // Page<Post>로 변환
//                .map(PostResponse::from);      // Page<PostResponse>로 변환
    }
}
