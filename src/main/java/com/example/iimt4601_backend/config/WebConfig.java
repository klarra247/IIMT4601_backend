package com.example.iimt4601_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:/Users/Klarra/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 파일 절대 경로에 맞게 수정
        String uploadPath = "file:" + uploadDir + "/";
        System.out.println("Upload path: " + uploadPath); // 로깅 추가

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}