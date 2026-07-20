package com.monika.payflow.transaction.service;

import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.transaction.entity.Transaction;
import com.monika.payflow.transaction.entity.TransactionStatus;
import com.monika.payflow.transaction.entity.TransactionType;
import com.monika.payflow.transaction.repository.TransactionRepository;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void recordDepositCreatesSuccessfulDepositTransaction() {
        Wallet wallet = wallet(UUID.randomUUID(), UUID.randomUUID(), "125.25");

        transactionService.recordDeposit(
                wallet,
                new BigDecimal("25.25"),
                new BigDecimal("100.00"),
                new BigDecimal("125.25")
        );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.walletId()).isEqualTo(wallet.id());
        assertThat(transaction.transactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transaction.amount()).isEqualByComparingTo("25.25");
        assertThat(transaction.balanceBefore()).isEqualByComparingTo("100.00");
        assertThat(transaction.balanceAfter()).isEqualByComparingTo("125.25");
        assertThat(transaction.currency()).isEqualTo("INR");
        assertThat(transaction.referenceNumber()).startsWith("TXN-");
        assertThat(transaction.description()).isEqualTo("Wallet deposit");
    }

    @Test
    void recordWithdrawalCreatesSuccessfulWithdrawalTransaction() {
        Wallet wallet = wallet(UUID.randomUUID(), UUID.randomUUID(), "60.00");

        transactionService.recordWithdrawal(
                wallet,
                new BigDecimal("40.00"),
                new BigDecimal("100.00"),
                new BigDecimal("60.00")
        );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.transactionType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(transaction.amount()).isEqualByComparingTo("40.00");
        assertThat(transaction.balanceBefore()).isEqualByComparingTo("100.00");
        assertThat(transaction.balanceAfter()).isEqualByComparingTo("60.00");
        assertThat(transaction.description()).isEqualTo("Wallet withdrawal");
    }

    @Test
    void recordTransferOutCreatesSuccessfulTransferOutTransaction() {
        Wallet wallet = wallet(UUID.randomUUID(), UUID.randomUUID(), "70.00");

        transactionService.recordTransferOut(
                wallet,
                new BigDecimal("30.00"),
                new BigDecimal("100.00"),
                new BigDecimal("70.00")
        );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.transactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transaction.amount()).isEqualByComparingTo("30.00");
        assertThat(transaction.balanceBefore()).isEqualByComparingTo("100.00");
        assertThat(transaction.balanceAfter()).isEqualByComparingTo("70.00");
        assertThat(transaction.description()).isEqualTo("Wallet transfer sent");
    }

    @Test
    void recordTransferInCreatesSuccessfulTransferInTransaction() {
        Wallet wallet = wallet(UUID.randomUUID(), UUID.randomUUID(), "50.00");

        transactionService.recordTransferIn(
                wallet,
                new BigDecimal("30.00"),
                new BigDecimal("20.00"),
                new BigDecimal("50.00")
        );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();
        assertThat(transaction.transactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(transaction.amount()).isEqualByComparingTo("30.00");
        assertThat(transaction.balanceBefore()).isEqualByComparingTo("20.00");
        assertThat(transaction.balanceAfter()).isEqualByComparingTo("50.00");
        assertThat(transaction.description()).isEqualTo("Wallet transfer received");
    }

    @Test
    void recordRejectsInvalidAmount() {
        Wallet wallet = wallet(UUID.randomUUID(), UUID.randomUUID(), "100.00");

        assertThatThrownBy(() -> transactionService.recordDeposit(
                wallet,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        )).isInstanceOf(BadRequestException.class);
    }

    @Test
    void getTransactionHistoryReturnsNewestFirstRepositoryResult() {
        UUID userId = UUID.randomUUID();
        Transaction transaction = transaction(wallet(userId, UUID.randomUUID(), "125.25"));
        when(transactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of(transaction));

        List<TransactionResponse> response = transactionService.getTransactionHistory(userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(transaction.id());
        assertThat(response.getFirst().walletId()).isEqualTo(transaction.walletId());
    }

    @Test
    void getTransactionRejectsTransactionOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findByIdAndWallet_User_Id(transactionId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(userId, transactionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Transaction transaction(Wallet wallet) {
        Transaction transaction = new Transaction(
                wallet,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                new BigDecimal("25.25"),
                new BigDecimal("100.00"),
                new BigDecimal("125.25"),
                "INR",
                "TXN-" + UUID.randomUUID(),
                "Wallet deposit"
        );
        ReflectionTestUtils.setField(transaction, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(transaction, "createdAt", Instant.now());
        return transaction;
    }

    private Wallet wallet(UUID userId, UUID walletId, String balance) {
        Wallet wallet = new Wallet(user(userId), new BigDecimal(balance), "INR", WalletStatus.ACTIVE);
        ReflectionTestUtils.setField(wallet, "id", walletId);
        return wallet;
    }

    private User user(UUID userId) {
        User user = new User("monika@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
