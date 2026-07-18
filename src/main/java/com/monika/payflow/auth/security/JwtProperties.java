package com.monika.payflow.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payflow.security.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {
}
