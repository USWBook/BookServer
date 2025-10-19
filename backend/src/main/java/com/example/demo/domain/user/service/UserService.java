package com.example.demo.domain.user.service;

import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.exception.PasswordNotEqualException;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
