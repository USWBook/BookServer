package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MajorRepository majorRepository;

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
        
        currentUser.updateProfile(request.name(), newMajor, Grade.fromValue(request.grade()), Semester.fromValue(request.semester()));

        return UserInfoResponse.from(currentUser);
    }

    private Major findMajorOrNull(UUID majorId) {
        if (majorId == null) {
            return null;
        }
        return majorRepository.findById(majorId)
                .orElseThrow(MajorNotFoundException::new);
    }


}
