package com.monika.payflow.wallet.service;

import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.transaction.service.TransactionRecorder;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.wallet.dto.WalletResponse;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import com.monika.payflow.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRecorder transactionRecorder;

    @InjectMocks
    private WalletService walletService;

    @Test
    void createWalletForUserCreatesActiveWalletWithZeroBalance() {
        User user = user(UUID.randomUUID());
        when(walletRepository.existsByUserId(user.id())).thenReturn(false);

        walletService.createWalletForUser(user);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet wallet = walletCaptor.getValue();
        assertThat(wallet.userId()).isEqualTo(user.id());
        assertThat(wallet.balance()).isEqualByComparingTo("0.00");
        assertThat(wallet.currency()).isEqualTo("INR");
        assertThat(wallet.status()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void createWalletForUserRejectsDuplicateWallet() {
        User user = user(UUID.randomUUID());
        when(walletRepository.existsByUserId(user.id())).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWalletForUser(user))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getWalletReturnsCurrentWalletDetails() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "125.50");
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getWallet(userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.balance()).isEqualByComparingTo("125.50");
    }

    @Test
    void getWalletRejectsMissingWallet() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWallet(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void depositIncreasesWalletBalance() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "100.00");
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.deposit(userId, new BigDecimal("25.25"));

        assertThat(response.balance()).isEqualByComparingTo("125.25");
        assertThat(wallet.balance()).isEqualByComparingTo("125.25");
        verify(transactionRecorder).recordDeposit(
                wallet,
                new BigDecimal("25.25"),
                new BigDecimal("100.00"),
                new BigDecimal("125.25")
        );
    }

    @Test
    void withdrawDecreasesWalletBalance() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "100.00");
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.withdraw(userId, new BigDecimal("40.00"));

        assertThat(response.balance()).isEqualByComparingTo("60.00");
        assertThat(wallet.balance()).isEqualByComparingTo("60.00");
        verify(transactionRecorder).recordWithdrawal(
                wallet,
                new BigDecimal("40.00"),
                new BigDecimal("100.00"),
                new BigDecimal("60.00")
        );
    }

    @Test
    void withdrawRejectsInsufficientBalance() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "20.00");
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(userId, new BigDecimal("25.00")))
                .isInstanceOf(BadRequestException.class);

        assertThat(wallet.balance()).isEqualByComparingTo("20.00");
        verifyNoInteractions(transactionRecorder);
    }

    @Test
    void depositRejectsInvalidAmount() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(userId, "20.00");
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.deposit(userId, new BigDecimal("0.001")))
                .isInstanceOf(BadRequestException.class);

        assertThat(wallet.balance()).isEqualByComparingTo("20.00");
        verifyNoInteractions(transactionRecorder);
    }

    private Wallet wallet(UUID userId, String balance) {
        return new Wallet(user(userId), new BigDecimal(balance), "INR", WalletStatus.ACTIVE);
    }

    private User user(UUID userId) {
        User user = new User("monika@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
