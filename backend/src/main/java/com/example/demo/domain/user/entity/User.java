package com.example.demo.domain.user.entity;

import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.user.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
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

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

}
