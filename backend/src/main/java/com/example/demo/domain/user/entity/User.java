package com.example.demo.domain.user.entity;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.user.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String email;

    private String password;

    private String name;

    private String studentId;

    private Major major;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public void completeSignUp() {
        this.status = UserStatus.ACTIVE;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

}
