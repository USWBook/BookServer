package com.example.demo.domain.user.entity;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semester semester;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;


    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public void withdraw() {

        String uniqueAnonymousId = "withdrawn_" + this.id.toString();

        this.name = "탈퇴한 사용자";
        this.password = uniqueAnonymousId;// 복구 불가능
        this.studentId = uniqueAnonymousId;

        this.major = null; // @ManyToOne 관계는 null로 설정하여 연결을 끊음.
        this.grade = Grade.GRADE_0;
        this.semester = Semester.Semester_0;
        this.role = Role.WITHDRAWAL;

        this.status = UserStatus.WITHDRAWAL;
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    public void updateProfile(String name, Major major, Grade grade, Semester semester) {
        if (name != null) this.name = name;
        if (major != null) this.major = major;
        if (grade != null) this.grade = grade;
        if (semester != null) this.semester = semester;
    }

}
