package com.example.demo.global.config.web;

import com.example.demo.global.converter.StringToGradeConverter;
import com.example.demo.global.converter.StringToSemesterConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToGradeConverter());
        registry.addConverter(new StringToSemesterConverter()); // Semester도 만들었다면 추가
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 파일 시스템의 업로드된 채팅 이미지 제공
        String absoluteUploadPath = java.nio.file.Paths.get(System.getProperty("user.dir"), "uploads", "chat-images").toString();
        String location = "file:" + (absoluteUploadPath.endsWith("/") || absoluteUploadPath.endsWith("\\") ? absoluteUploadPath : absoluteUploadPath + "/");
        registry
                .addResourceHandler("/chat-images/**")
                .addResourceLocations(location);
    }
}

