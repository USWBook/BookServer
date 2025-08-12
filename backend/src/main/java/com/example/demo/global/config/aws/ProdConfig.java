package com.example.demo.global.config.aws;

import com.example.demo.global.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
@Profile("prod")
public class ProdConfig {

    @Bean
    public SecretsManagerClient secretsManagerClient() {
        // DefaultCredentialsProvider는 애플리케이션이 실행되는 환경(예: EC2, ECS)의
        // IAM 역할을 자동으로 감지하여 자격 증명을 가져옵니다.
        // 따라서 코드에 Access Key를 넣을 필요가 없습니다.
        return SecretsManagerClient.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public JwtProvider jwtProvider(SecretsManagerClient secretsManagerClient,
                                   @Value("${jwt.secret-name}") String secretName,
                                   @Value("${jwt.access-expiration}") long accessExpiration,
                                   @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        // AWS SecretsManagerClient를 사용하여 JwtProvider를 생성
        return new JwtProvider(secretsManagerClient, secretName, accessExpiration, refreshExpiration);
    }
}
