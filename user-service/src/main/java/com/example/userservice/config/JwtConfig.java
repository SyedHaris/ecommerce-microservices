package com.example.userservice.config;

import com.example.userservice.util.JwtUtil;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(Keys.hmacShaKeyFor(secretKey.getBytes()));
    }

}
