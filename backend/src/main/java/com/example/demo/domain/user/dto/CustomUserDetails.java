package com.example.demo.domain.user.dto;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.role.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final Role role;
    private final UserStatus status;

    // 1. (DB 조회 시 사용) User 엔티티 전체를 받는 생성자
    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    // 2. (JWT 필터에서 사용) 토큰 정보만으로 생성하는 생성자
    public CustomUserDetails(String email, Role role) {
        this.email = email;
        this.password = null; // 토큰 기반 인증 시에는 비밀번호가 필요 없음
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
        }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE;
    }

}