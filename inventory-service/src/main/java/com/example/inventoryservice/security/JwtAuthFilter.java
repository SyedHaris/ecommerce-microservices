package com.example.inventoryservice.security;

import com.example.inventoryservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TYPE = "Bearer";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, java.io.IOException {
        final String token = extractAuthorizationHeader(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String tokenUser = jwtUtil.extractUserId(token);

        if (tokenUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.isTokenExpired(token)) {
                throw new BadCredentialsException("Token is not valid");
            }

            List<String> roles = Arrays.stream(jwtUtil.extractRoles(token).split(", ")).toList();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(tokenUser, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }

    private String extractAuthorizationHeader(HttpServletRequest request) {
        final String headerValue = request.getHeader(AUTH_HEADER);

        if (headerValue == null || !headerValue.startsWith(AUTH_TYPE)) {
            return null;
        }

        return headerValue.substring(AUTH_TYPE.length()).trim();
    }
}
