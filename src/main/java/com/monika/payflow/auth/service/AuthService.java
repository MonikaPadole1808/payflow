package com.monika.payflow.auth.service;

import com.monika.payflow.auth.dto.AuthResponse;
import com.monika.payflow.auth.dto.LoginRequest;
import com.monika.payflow.auth.dto.RefreshTokenRequest;
import com.monika.payflow.auth.dto.RegisterRequest;
import com.monika.payflow.auth.entity.RefreshToken;
import com.monika.payflow.auth.repository.RefreshTokenRepository;
import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.auth.security.JwtProperties;
import com.monika.payflow.auth.security.JwtService;
import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.AuthenticationFailedException;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.InvalidRefreshTokenException;
import com.monika.payflow.notification.service.NotificationRecorder;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.service.UserAccountService;
import com.monika.payflow.wallet.service.WalletProvisioningService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserAccountService userAccountService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final WalletProvisioningService walletProvisioningService;
    private final NotificationRecorder notificationRecorder;

    public AuthService(
            UserAccountService userAccountService,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            WalletProvisioningService walletProvisioningService,
            NotificationRecorder notificationRecorder
    ) {
        this.userAccountService = userAccountService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.walletProvisioningService = walletProvisioningService;
        this.notificationRecorder = notificationRecorder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userAccountService.existsByEmail(normalizedEmail)) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS.defaultMessage());
        }

        User savedUser = userAccountService.createActiveUser(
                normalizedEmail,
                passwordEncoder.encode(request.password())
        );
        walletProvisioningService.createWalletForUser(savedUser);
        notificationRecorder.recordRegistration(savedUser.id());
        return createAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (BadCredentialsException exception) {
            throw new AuthenticationFailedException(ErrorCode.INVALID_CREDENTIALS.defaultMessage());
        } catch (AuthenticationException exception) {
            throw new AuthenticationFailedException(ErrorCode.INVALID_CREDENTIALS.defaultMessage());
        }

        User user = userAccountService.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException(ErrorCode.INVALID_CREDENTIALS.defaultMessage()));

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        return createAuthResponse(refreshToken.user());
    }

    private AuthResponse createAuthResponse(User user) {
        AuthUserDetails userDetails = new AuthUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.token(), TOKEN_TYPE);
    }

    private RefreshToken createRefreshToken(User user) {
        Instant expiresAt = Instant.now().plusSeconds(jwtProperties.refreshTokenExpirationDays() * 24 * 60 * 60);
        RefreshToken refreshToken = new RefreshToken(user, UUID.randomUUID().toString(), expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

}
