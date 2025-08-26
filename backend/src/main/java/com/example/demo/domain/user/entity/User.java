package com.example.demo.domain.user.entity;

import com.example.demo.domain.major.entity.Major;
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String studentId;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false)
    private Integer semester;


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


    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    public void updateProfile(String name, Major major, Integer grade, Integer semester) {
        if (name != null) this.name = name;
        if (major != null) this.major = major;
        if (grade != null) this.grade = grade;
        if (semester != null) this.semester = semester;
    }

}
