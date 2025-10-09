package com.example.demo.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisAuthConfig {

    @Bean(name = "authRedisTemplate")
    public StringRedisTemplate authRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        // StringRedisTemplate은 기본적으로 Key와 Value에 StringRedisSerializer를 사용하므로
        // 별도의 Serializer 설정이 필요 없음.
        return template;
    }
}
