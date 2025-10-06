package com.example.demo.global.security.userdetails;

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
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Role role;
    private final UserStatus status;

    // 1. (DB 조회 시 사용) User 엔티티 전체를 받는 생성자
    // 클라이언트 로그인 요청시
    // [LoginAuthenticationFilter] 에서 ID/PW로 토큰 생성
    // authenticationManager.authenticate(토큰) 호출
    // [AuthenticationManager] 가 [DaoAuthenticationProvider] 에게 위임
    // [DaoAuthenticationProvider] 는 내가가 등록한 [CustomUserDetailsService] 를 사용
    // CustomUserDetailsService.loadUserByUsername(이메일) 호출
    // DB에서 사용자 정보 조회 후 [CustomUserDetails] 반환
    // [DaoAuthenticationProvider] 가 비밀번호 및 밴 여부 비교 후 인증 완료
    //인증 성공/실패 핸들러 실행
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    // 2. (JWT 필터에서 사용) 토큰 정보만으로 생성하는 생성자
    public CustomUserDetails(UUID id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.password = null; // 토큰 기반 인증 시에는 비밀번호가 필요 없음
        this.role = role;
        this.status = UserStatus.ACTIVE; // 토큰이 유효하다면 사용자는 활성 상태로 간주
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