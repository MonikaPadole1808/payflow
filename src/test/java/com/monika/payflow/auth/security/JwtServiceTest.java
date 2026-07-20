package com.monika.payflow.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes());

    @Test
    void generatedTokenCanBeValidatedForSameUser() {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 15, 7));
        UserDetails userDetails = User.withUsername("monika@example.com")
                .password("encoded")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateAccessToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("monika@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService jwtService = new JwtService(new JwtProperties(SECRET, 0, 7));
        UserDetails userDetails = User.withUsername("monika@example.com")
                .password("encoded")
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateAccessToken(userDetails);
        Thread.sleep(1000);

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }
}
