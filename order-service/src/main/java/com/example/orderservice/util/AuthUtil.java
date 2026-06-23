package com.example.orderservice.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static Long getAuthenticatedUserId() {
        return Long.valueOf((String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal());
    }

}
