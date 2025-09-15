package com.example.demo.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableAsync // @Async 어노테이션을 사용하려면 반드시 필요합니다.
public class AsyncConfig implements AsyncConfigurer {

    /**
     * @Async 메소드에서 발생하는 예외를 처리할 핸들러를 반환합니다.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async method '" + method.getName() + "' threw an exception with params: "
                        + Arrays.toString(params), ex);
    }
}
