package com.example.demo.global.security.filter;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.exception.BannedUserException;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

//@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("허용되지 않은 요청 방식입니다. POST 메서드를 사용해 주십시오.");
        }

        try (ServletInputStream is = request.getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(is, LoginRequest.class);

            String email = loginRequest.email();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(email));

            if (user.getStatus() == UserStatus.BANNED) {
                throw new LockedException("밴된 계정입니다.");
            }

            if (user.getStatus() == UserStatus.WITHDRAWAL) {
                throw new DisabledException("탈퇴한 계정입니다.");
            }

            /*
            UsernamePasswordAuthenticationToken은 Authentication 인터페이스의 구현체로,
            누가(principal), 무엇으로(credentials) 인증을 시도하는가 에 대한 정보를 담는 그릇
            이 시점의 authRequest 객체는 아직 인증되지 않은 상태(unauthenticated)
            AuthenticationManager에게 인증 받아야함
             */
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, loginRequest.password());

            /*
            UsernamePasswordAuthenticationFilter의 부모 클래스에 있는 메서드를 호출.
            이 메서드는 생성된 임시 출입증(authRequest)에 부가적인 정보를 추가.
            주로 IP 주소, 세션 ID 같은 상세 정보가 담김.
             */
            setDetails(request, authRequest);

            /*
            AuthenticationManager에게 임시 출입증(authRequest)이 진짜인지 검사하라고 위임(delegate)하는 과정.
            AuthenticationManager는 직접 일하지 않고, (AuthenticationProvider)에게 일을 넘기는 총괄 매니저 역할.
            AuthenticationManager는 authRequest (즉, UsernamePasswordAuthenticationToken)를 처리할 수 있는 AuthenticationProvider를 찾음.
            일반적으로 DaoAuthenticationProvider가 이 역할을 담당.
            DaoAuthenticationProvider는 다음과 같은 일을 순서대로 진행.
            사용자 정보 조회: UserDetailsService를 사용해 authRequest에 담긴 이메일(principal)로 데이터베이스에서 실제 사용자 정보(UserDetails)를 찾아옴.(UserDetailsService.loadUserByUsername)
            비밀번호 비교: PasswordEncoder를 사용해 authRequest에 담긴 사용자가 입력한 비밀번호(credentials)와 DB에서 조회한 암호화된 비밀번호가 일치하는지 비교.
            결과 반환:
            인증 성공 시: 비밀번호가 일치하면, 새로운 UsernamePasswordAuthenticationToken 객체를 생성. 이 새로운 객체는 사용자 정보(UserDetails)와 권한(GrantedAuthority) 정보를 담고 있으며, 인증된 상태(authenticated)로 설정. 이 객체가 최종적으로 반환.
            인증 실패 시: 사용자 정보가 없거나 비밀번호가 틀리면 AuthenticationException (예: BadCredentialsException) 예외를 발생.
             */
            //  인증 시도
            Authentication authentication = authenticationManager.authenticate(authRequest);

            //  인증 성공 직후, 비밀번호 업그레이드 로직 호출
            // 이때 user는 준영속 상태
            if (authentication.isAuthenticated()) {
                authService.upgradePasswordIfNecessary(user, loginRequest.password());
            }

            return authentication;
        } catch (IOException e) {
            throw new AuthenticationServiceException("인증 요청 본문을 처리하는 데 실패했습니다.", e);
        }
    }
}