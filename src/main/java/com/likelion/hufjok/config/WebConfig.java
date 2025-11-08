package com.likelion.hufjok.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

public class WebConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory f = new MultipartConfigFactory();
        f.setMaxFileSize(DataSize.ofMegabytes(200));      // 파일 200MB
        f.setMaxRequestSize(DataSize.ofMegabytes(200));   // 요청 200MB
        return f.createMultipartConfig();
    }

    // 혹시 CommonsMultipartResolver가 잡히는 걸 방지하고 Standard로 고정
    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
