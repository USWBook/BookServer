package com.example.demo.global.annotation.swagger;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorResponses {
    ApiErrorResponse[] value();
}
