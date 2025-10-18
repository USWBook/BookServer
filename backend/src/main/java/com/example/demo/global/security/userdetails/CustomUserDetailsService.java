package com.example.demo.global.security.userdetails;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 이메일을 기반으로 사용자 정보를 조회.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 찾을 수 없습니다: " + email));

        //  정지된 사용자인지 확인
        if (user.getStatus() == UserStatus.BANNED) {
            // LockedException을 던지면 Spring Security가 "계정이 잠김"으로 처리
            throw new LockedException("밴된 계정입니다.");
        }

        //  탈퇴한 사용자인지 확인 -> 어차피 비밀번호를 난수로 교체해 버려서 로그인 가능성은 아주 희박함
        if (user.getStatus() == UserStatus.WITHDRAWAL) {
            // DisabledException을 던지면 Spring Security가 "계정이 비활성화됨"으로 처리
            throw new DisabledException("탈퇴한 계정입니다.");
        }

        //  User 엔티티를 사용해 새로운 UserPrincipal(완제품)을 생성하여 반환.
        return new CustomUserDetails(user);
    }
}
