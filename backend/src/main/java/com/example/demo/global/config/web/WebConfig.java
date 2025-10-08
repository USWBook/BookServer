package com.example.demo.global.config.web;

import com.example.demo.global.converter.StringToGradeConverter;
import com.example.demo.global.converter.StringToSemesterConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToGradeConverter());
        registry.addConverter(new StringToSemesterConverter()); // Semester도 만들었다면 추가
    }
}

