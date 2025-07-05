package com.example.demo.global.security;

import java.util.List;

public class SecurityConstants {
    public static final List<String> AUTH_WHITELIST =
            List.of(
                    "/api/posts/**"

            );
}
