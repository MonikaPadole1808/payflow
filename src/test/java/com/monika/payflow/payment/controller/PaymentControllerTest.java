package com.monika.payflow.payment.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.payment.dto.PaymentTransferRequest;
import com.monika.payflow.payment.entity.PaymentStatus;
import com.monika.payflow.payment.service.PaymentService;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void transferReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        PaymentTransferRequest request = new PaymentTransferRequest(
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                "Dinner"
        );
        PaymentResponse payment = paymentResponse(UUID.randomUUID());
        when(paymentService.transfer(userId, request)).thenReturn(payment);

        ResponseEntity<ApiResponse<PaymentResponse>> response = paymentController.transfer(currentUser, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Payment transfer completed successfully");
        assertThat(response.getBody().data()).isEqualTo(payment);
    }

    @Test
    void getPaymentHistoryReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        PaymentResponse payment = paymentResponse(UUID.randomUUID());
        when(paymentService.getPaymentHistory(userId)).thenReturn(List.of(payment));

        ResponseEntity<ApiResponse<List<PaymentResponse>>> response =
                paymentController.getPaymentHistory(currentUser);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Payments retrieved successfully");
        assertThat(response.getBody().data()).containsExactly(payment);
    }

    @Test
    void getPaymentReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        PaymentResponse payment = paymentResponse(paymentId);
        when(paymentService.getPayment(userId, paymentId)).thenReturn(payment);

        ResponseEntity<ApiResponse<PaymentResponse>> response =
                paymentController.getPayment(currentUser, paymentId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Payment retrieved successfully");
        assertThat(response.getBody().data()).isEqualTo(payment);
    }

    private AuthUserDetails currentUser(UUID userId) {
        User user = new User("monika@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return new AuthUserDetails(user);
    }

    private PaymentResponse paymentResponse(UUID paymentId) {
        return new PaymentResponse(
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("30.00"),
                "INR",
                PaymentStatus.SUCCESS,
                "PAY-" + UUID.randomUUID(),
                "Wallet transfer",
                Instant.now()
        );
    }
}
