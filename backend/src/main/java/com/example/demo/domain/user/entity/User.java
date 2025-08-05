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
public class User implements UserDetails {

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한을 반환합니다. 여기서는 Role을 기반으로 생성합니다.
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security에서 사용자를 식별하는 값으로 사용됩니다. 이메일을 ID로 사용합니다.
        return this.email;
    }

    @Override
    public String getPassword() {
        // Spring Security가 비밀번호를 비교할 때 사용합니다.
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부. 만료 정책이 없다면 true를 반환합니다.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부. BANNED 상태일 때 잠긴 것으로 처리합니다.
        return this.status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명(비밀번호) 만료 여부. 만료 정책이 없다면 true를 반환합니다.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부. ACTIVE 상태일 때 활성화된 것으로 처리합니다.
        return this.status == UserStatus.ACTIVE;
    }

}
