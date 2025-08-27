package com.example.demo.global.config.redis;

import com.example.demo.global.config.chat.RedisSubscriber;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

// Redis 설정 클래스임을 명시
@Configuration

// "test" 프로파일 환경에서는 이 설정을 제외하도록 함
@Profile("!test")

// Lombok 어노테이션으로 final 필드 생성자 자동 생성
@RequiredArgsConstructor

// Redis Repository 활성화 및 keyspace 이벤트 감지 시작 시 설정
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedischatConfig {

    // application.properties 또는 환경변수에서 Redis 서버 호스트를 주입, 없는 경우 "localhost"로 기본값 설정
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    // Redis 서버 포트 주입, 기본값 6379
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Redis 서버와의 연결 팩토리 빈 생성
     * LettuceConnectionFactory: Lettuce 클라이언트를 사용하여 Redis 연결 구성
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * Redis에 문자열 작업을 간단히 처리할 수 있는 StringRedisTemplate 빈 생성
     * RedisConnectionFactory를 주입받아 생성
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    /**
     * Redis Pub/Sub용 단일 토픽 설정을 위한 Bean
     * "chatroom" 이라는 토픽 명 지정
     */
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    /**
     * Redis로 발행된 메시지를 수신하기 위한 리스너 컨테이너 설정
     * 연결 팩토리, 메시지 리스너, 토픽을 주입 받아 관리
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter,
                                                              ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory); // Redis 연결 설정
        container.addMessageListener(listenerAdapter, channelTopic); // 해당 토픽에 대한 리스너 등록
        return container;
    }

    /**
     * 실제 Redis에서 수신한 메시지를 처리하는 Subscriber 어댑터 설정
     * RedisSubscriber 객체의 sendMessage 메서드를 메시지 수신 시 호출하도록 연결
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }

    /**
     * Redis에서 ChatRoom 객체를 JSON 직렬화 및 역직렬화하며 저장/조회 할 수 있도록 설정된 RedisTemplate
     * - 키는 문자열로 직렬화(StringRedisSerializer)
     * - 값은 JSON 포맷으로 직렬화(GenericJackson2JsonRedisSerializer)
     * - ObjectMapper에 Java 8 시간 지원 및 타입 정보 포함 설정 등 커스텀 모듈 적용
     */
    @Bean(name = "chatRedisTemplate")
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜/시간 타입 모듈 등록 (LocalDateTime 등)
        objectMapper.registerModule(new JavaTimeModule());

        // JSON 직렬화 시 타입 정보를 포함하기 위한 설정 (보안 상 LaissezFaireSubTypeValidator 사용)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        // 날짜 데이터를 타임스탬프로 저장하지 않고 읽기쉬운 ISO-8601 문자열로 저장
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 위 ObjectMapper를 이용한 GenericJackson2JsonRedisSerializer 생성
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // RedisTemplate에 직렬화기 등록
        redisTemplate.setKeySerializer(stringSerializer);         // 키 직렬화
        redisTemplate.setHashKeySerializer(stringSerializer);     // 해시 키 직렬화
        redisTemplate.setValueSerializer(jsonSerializer);         // 값 직렬화
        redisTemplate.setHashValueSerializer(jsonSerializer);     // 해시 값 직렬화

        // 빈 생성 후 초기화
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Redis에서 문자열 기반 데이터만 다룰 경우 사용하는 RedisTemplate 설정
     * 모든 키와 값, 해시 키와 값 직렬화에 StringRedisSerializer 사용 (문자열 처리 전용)
     */
    @Bean(name = "chatStringRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 모든 키/값에 대해 문자열 직렬화 적용
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // 빈 초기화
        template.afterPropertiesSet();
        return template;
    }
}
