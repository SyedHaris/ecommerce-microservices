package com.example.orderservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

import java.util.function.Function;

public class JwtUtil {

    private final SecretKey secretKey;

    private static final String ROLES = "roles";

    private static final String EMAIL = "email";

    public JwtUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String extractUserId(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractCreatedAt(String token) throws JwtException {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public String extractRoles(String token) throws JwtException {
        return extractClaim(token, (Claims claims) -> claims.get(ROLES, String.class));
    }

    public String extractEmail(String token) throws JwtException {
        return extractClaim(token, (Claims claims) -> claims.get(EMAIL, String.class));
    }

    public boolean isTokenExpired(String token) throws JwtException {
        return extractExpirationDate(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) throws JwtException {
        final Claims claims = extractClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}