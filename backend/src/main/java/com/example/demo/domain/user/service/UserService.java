package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.response.UserInfoResponse;
import com.example.demo.global.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final MajorRepository majorRepository;

    public UserInfoResponse getUserInfo(String authHeader) {
        User user = userRepository.findByEmail(jwtProvider.extractEmail(authHeader.replace("Bearer ", "")))
                .orElseThrow(UserNotFoundException::new);


        return new UserInfoResponse(
                user.getName(),
                user.getMajor().getName(),
                user.getEmail(),
                user.getGrade(),
                user.getSemester()
        );
    }

    @Transactional
    public void changeInformation(ChangeInfoRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(MemberNotFoundException::new);

        updateFieldIfNotNull(request.grade(), currentUser::setGrade);
        updateFieldIfNotNull(request.semester(), currentUser::setSemester);

        if (request.majorName() != null) {
            Major newMajor = majorRepository.findByName(request.majorName())
                    .orElseThrow(MajorNotFoundException::new);
            currentUser.setMajor(newMajor);
        }
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> updater) {
        Optional.ofNullable(value).ifPresent(updater);
    }
}
