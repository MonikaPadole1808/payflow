package com.monika.payflow.auth.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "payflow.security.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {

    public JwtProperties {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("payflow.security.jwt.secret must be configured");
        }

        try {
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (DecodingException | WeakKeyException exception) {
            throw new IllegalStateException(
                    "payflow.security.jwt.secret must be a Base64-encoded HMAC key of at least 256 bits",
                    exception
            );
        }
    }
}
