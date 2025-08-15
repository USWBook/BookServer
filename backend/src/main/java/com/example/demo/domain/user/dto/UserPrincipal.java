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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;
//    private final UUID id;
//    private final String email;
//    private final String password;
//    private final Collection<? extends GrantedAuthority> authorities;
//    private final UserStatus status;
//    private final Role role;

//    // 생성자: User 엔티티를 받아서 UserDetails로 필요한 정보만 가공합니다.
//    public UserPrincipal(User user) {
//        this.id = user.getId();
//        this.email = user.getEmail();
//        this.password = user.getPassword();
//        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
//        this.status = user.getStatus();
//        this.role = user.getRole();
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

    public Role getRole() {
        return user.getRole();
    }
}