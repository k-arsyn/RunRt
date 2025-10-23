package com.runrt.gateway.config;

import com.runrt.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${gateway.jwt.base64secret:dGVzdC1nYXRld2F5LXNlY3JldA==}")
    private String base64Secret;

    @Value("${gateway.jwt.expirationms:86400000}")
    private long expirationMs;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(base64Secret, expirationMs);
    }
}

