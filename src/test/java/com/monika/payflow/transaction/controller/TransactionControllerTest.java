package com.monika.payflow.transaction.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.transaction.entity.TransactionStatus;
import com.monika.payflow.transaction.entity.TransactionType;
import com.monika.payflow.transaction.service.TransactionService;
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
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getTransactionHistoryReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        TransactionResponse transaction = transactionResponse(UUID.randomUUID());
        when(transactionService.getTransactionHistory(userId)).thenReturn(List.of(transaction));

        ResponseEntity<ApiResponse<List<TransactionResponse>>> response =
                transactionController.getTransactionHistory(currentUser);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Transactions retrieved successfully");
        assertThat(response.getBody().data()).containsExactly(transaction);
    }

    @Test
    void getTransactionReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        TransactionResponse transaction = transactionResponse(transactionId);
        when(transactionService.getTransaction(userId, transactionId)).thenReturn(transaction);

        ResponseEntity<ApiResponse<TransactionResponse>> response =
                transactionController.getTransaction(currentUser, transactionId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Transaction retrieved successfully");
        assertThat(response.getBody().data()).isEqualTo(transaction);
    }

    private AuthUserDetails currentUser(UUID userId) {
        User user = new User("monika@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return new AuthUserDetails(user);
    }

    private TransactionResponse transactionResponse(UUID transactionId) {
        return new TransactionResponse(
                transactionId,
                UUID.randomUUID(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                new BigDecimal("25.00"),
                new BigDecimal("100.00"),
                new BigDecimal("125.00"),
                "INR",
                "TXN-" + UUID.randomUUID(),
                "Wallet deposit",
                Instant.now()
        );
    }
}
