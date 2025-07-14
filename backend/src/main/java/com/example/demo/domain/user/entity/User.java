package com.example.demo.domain.user.entity;

import com.example.demo.domain.user.role.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

}
