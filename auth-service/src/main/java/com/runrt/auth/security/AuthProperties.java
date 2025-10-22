package com.runrt.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthProperties {
    private String jwtBase64Secret;
    private long jwtExpirationMs = 86400000; // 1 day
}
