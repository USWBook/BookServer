package com.example.demo.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync // @Async 어노테이션 활성화
public class AsyncConfig implements AsyncConfigurer {

    /**
     * @Async 메서드에서 발생하는 예외를 처리할 핸들러를 설정한다.
     * 이를 통해 비동기 작업 중 발생한 예외가 무시되지 않고, 에러 로그로 기록될 수 있도록 보장한다.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async method '" + method.getName() + "' threw an exception with params: "
                        + Arrays.toString(params), ex);
    }

    /**
     * 이메일 발송 전용 스레드 풀을 생성하고 "EmailThreadPoolTaskExecutor"라는 이름의 Bean으로 등록한다.
     */
    @Bean(name = "EmailThreadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);      // 기본 스레드 수
        executor.setMaxPoolSize(20);      // 최대 스레드 수
        executor.setQueueCapacity(500);   // 대기 큐 크기
        executor.setThreadNamePrefix("MailExecutor-"); // 스레드 이름 접두사
        executor.setWaitForTasksToCompleteOnShutdown(true); // 앱 종료 시 큐에 남은 작업 완료 대기
        executor.setAwaitTerminationSeconds(60); // 최대 60초까지 대기

        executor.initialize();
        return executor;
    }
}
