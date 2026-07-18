package com.monika.payflow.auth.service;

import com.monika.payflow.auth.dto.AuthResponse;
import com.monika.payflow.auth.dto.LoginRequest;
import com.monika.payflow.auth.dto.RefreshTokenRequest;
import com.monika.payflow.auth.dto.RegisterRequest;
import com.monika.payflow.auth.entity.RefreshToken;
import com.monika.payflow.auth.repository.RefreshTokenRepository;
import com.monika.payflow.auth.security.JwtProperties;
import com.monika.payflow.auth.security.JwtService;
import com.monika.payflow.common.exception.AuthenticationFailedException;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.InvalidRefreshTokenException;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.service.UserAccountService;
import com.monika.payflow.wallet.service.WalletProvisioningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private WalletProvisioningService walletProvisioningService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWithEncodedPasswordAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("Monika@Example.com", "password123");
        User savedUser = activeUser("monika@example.com", "encoded");

        when(userAccountService.existsByEmail("monika@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userAccountService.createActiveUser("monika@example.com", "encoded")).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtProperties.refreshTokenExpirationDays()).thenReturn(7L);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        verify(userAccountService).createActiveUser("monika@example.com", "encoded");
        verify(walletProvisioningService).createWalletForUser(savedUser);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userAccountService.existsByEmail("monika@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("monika@example.com", "password123")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginRejectsInvalidCredentials() {
        LoginRequest request = new LoginRequest("monika@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void refreshRejectsExpiredToken() {
        User user = activeUser("monika@example.com", "encoded");
        RefreshToken token = new RefreshToken(user, "refresh-token", Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("refresh-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    private User activeUser(String email, String password) {
        return new User(email, password, UserRole.USER, UserStatus.ACTIVE);
    }
}
