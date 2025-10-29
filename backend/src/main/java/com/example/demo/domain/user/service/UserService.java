package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.repository.PostLikeRepository;
import com.example.demo.domain.post.repository.PostRepository;
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


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return UserInfoResponse.from(user);
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
    public void changeProfileImage(UUID userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateProfileImage(imageUrl);
        // JPA 더티 체킹에 의해 트랜잭션 커밋 시 반영됩니다.
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
}
