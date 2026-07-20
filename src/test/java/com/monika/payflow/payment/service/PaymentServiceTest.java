package com.monika.payflow.payment.service;

import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.notification.service.NotificationRecorder;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.payment.dto.PaymentTransferRequest;
import com.monika.payflow.payment.entity.Payment;
import com.monika.payflow.payment.entity.PaymentStatus;
import com.monika.payflow.payment.repository.PaymentRepository;
import com.monika.payflow.transaction.service.TransactionRecorder;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import com.monika.payflow.wallet.service.WalletBalanceChange;
import com.monika.payflow.wallet.service.WalletTransferResult;
import com.monika.payflow.wallet.service.WalletTransferService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletTransferService walletTransferService;

    @Mock
    private TransactionRecorder transactionRecorder;

    @Mock
    private NotificationRecorder notificationRecorder;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void transferCreatesPaymentAndTwoTransferLedgerEntries() {
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();
        Wallet senderWallet = wallet(senderUserId, UUID.randomUUID(), "70.00");
        Wallet receiverWallet = wallet(receiverUserId, UUID.randomUUID(), "50.00");
        WalletBalanceChange senderChange = new WalletBalanceChange(
                senderWallet,
                new BigDecimal("30.00"),
                new BigDecimal("100.00"),
                new BigDecimal("70.00")
        );
        WalletBalanceChange receiverChange = new WalletBalanceChange(
                receiverWallet,
                new BigDecimal("30.00"),
                new BigDecimal("20.00"),
                new BigDecimal("50.00")
        );
        PaymentTransferRequest request = new PaymentTransferRequest(receiverUserId, new BigDecimal("30.00"), "Dinner");
        when(walletTransferService.transferBetweenUsers(senderUserId, receiverUserId, new BigDecimal("30.00")))
                .thenReturn(new WalletTransferResult(senderChange, receiverChange));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> persistedPayment(invocation.getArgument(0)));

        PaymentResponse response = paymentService.transfer(senderUserId, request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment payment = paymentCaptor.getValue();
        assertThat(payment.senderWalletId()).isEqualTo(senderWallet.id());
        assertThat(payment.receiverWalletId()).isEqualTo(receiverWallet.id());
        assertThat(payment.amount()).isEqualByComparingTo("30.00");
        assertThat(payment.currency()).isEqualTo("INR");
        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.referenceNumber()).startsWith("PAY-");
        assertThat(payment.description()).isEqualTo("Dinner");
        verify(transactionRecorder).recordTransferOut(
                senderWallet,
                new BigDecimal("30.00"),
                new BigDecimal("100.00"),
                new BigDecimal("70.00")
        );
        verify(transactionRecorder).recordTransferIn(
                receiverWallet,
                new BigDecimal("30.00"),
                new BigDecimal("20.00"),
                new BigDecimal("50.00")
        );
        verify(notificationRecorder).recordPaymentSent(senderUserId, new BigDecimal("30.00"));
        verify(notificationRecorder).recordPaymentReceived(receiverUserId, new BigDecimal("30.00"));
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void transferRejectsSelfTransferBeforeUpdatingWallets() {
        UUID userId = UUID.randomUUID();
        PaymentTransferRequest request = new PaymentTransferRequest(userId, new BigDecimal("30.00"), "Self");

        assertThatThrownBy(() -> paymentService.transfer(userId, request))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(walletTransferService, paymentRepository, transactionRecorder, notificationRecorder);
    }

    @Test
    void getPaymentHistoryReturnsUserPayments() {
        UUID userId = UUID.randomUUID();
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID());
        when(paymentRepository.findUserPaymentsOrderByCreatedAtDesc(userId)).thenReturn(List.of(payment));

        List<PaymentResponse> response = paymentService.getPaymentHistory(userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(payment.id());
    }

    @Test
    void getPaymentRejectsPaymentOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findUserPaymentById(paymentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(userId, paymentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Payment persistedPayment(Payment payment) {
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment, "createdAt", Instant.now());
        return payment;
    }

    private Payment payment(UUID senderUserId, UUID receiverUserId) {
        Payment payment = new Payment(
                wallet(senderUserId, UUID.randomUUID(), "100.00"),
                wallet(receiverUserId, UUID.randomUUID(), "50.00"),
                new BigDecimal("30.00"),
                "INR",
                PaymentStatus.SUCCESS,
                "PAY-" + UUID.randomUUID(),
                "Wallet transfer"
        );
        return persistedPayment(payment);
    }

    private Wallet wallet(UUID userId, UUID walletId, String balance) {
        Wallet wallet = new Wallet(user(userId), new BigDecimal(balance), "INR", WalletStatus.ACTIVE);
        ReflectionTestUtils.setField(wallet, "id", walletId);
        return wallet;
    }

    private User user(UUID userId) {
        User user = new User("monika-" + userId + "@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
