package com.example.userservice.util;

import com.example.userservice.constans.UserRole;
import com.example.userservice.entity.User;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(Keys.hmacShaKeyFor(
            "ZLYicY6d2g4svUSbv3lb68G0ZPE9ph8t6Xu3jwYfcJO".getBytes(StandardCharsets.UTF_8)
    ));

    @Test
    void generatesAndExtractsClaims() {
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername("haris@example.com")
                .password("encoded")
                .roles("USER")
                .build();
        User user = new User(42L, "haris", "haris@example.com", "encoded", UserRole.USER);

        String token = jwtUtil.generate(userDetails, user, 60000);

        assertThat(jwtUtil.extractUserId(token)).isEqualTo("42");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("haris@example.com");
        assertThat(jwtUtil.extractRoles(token)).isEqualTo("ROLE_USER");
        assertThat(jwtUtil.extractCreatedAt(token)).isBeforeOrEqualTo(jwtUtil.extractExpirationDate(token));
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }
}
