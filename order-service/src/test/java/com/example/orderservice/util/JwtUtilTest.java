package com.example.orderservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "ZLYicY6d2g4svUSbv3lb68G0ZPE9ph8t6Xu3jwYfcJO";
    private final JwtUtil jwtUtil = new JwtUtil(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)));

    @Test
    void extractsJwtClaims() {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + 60000);
        String token = Jwts.builder()
                .subject("42")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claims(Map.of("roles", "ROLE_USER", "email", "haris@example.com"))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtUtil.extractUserId(token)).isEqualTo("42");
        assertThat(jwtUtil.extractRoles(token)).isEqualTo("ROLE_USER");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("haris@example.com");
        assertThat(Math.abs(jwtUtil.extractCreatedAt(token).getTime() - issuedAt.getTime())).isLessThan(1000L);
        assertThat(Math.abs(jwtUtil.extractExpirationDate(token).getTime() - expiration.getTime())).isLessThan(1000L);
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }
}
