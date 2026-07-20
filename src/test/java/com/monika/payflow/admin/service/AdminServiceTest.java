package com.monika.payflow.admin.service;

import com.monika.payflow.admin.dto.AdminDashboardResponse;
import com.monika.payflow.admin.dto.AdminUserUpdateRequest;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.notification.service.NotificationService;
import com.monika.payflow.payment.service.PaymentService;
import com.monika.payflow.transaction.service.TransactionService;
import com.monika.payflow.user.dto.UserAdminResponse;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.service.UserAccountService;
import com.monika.payflow.wallet.dto.WalletResponse;
import com.monika.payflow.wallet.entity.WalletStatus;
import com.monika.payflow.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private WalletService walletService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getDashboardAggregatesStatisticsFromOwnedModuleServices() {
        when(userAccountService.countUsers()).thenReturn(10L);
        when(userAccountService.countActiveUsers()).thenReturn(8L);
        when(userAccountService.countBlockedUsers()).thenReturn(2L);
        when(walletService.countWallets()).thenReturn(10L);
        when(walletService.getTotalWalletBalance()).thenReturn(new BigDecimal("1250.50"));
        when(paymentService.countPayments()).thenReturn(7L);
        when(paymentService.countPaymentsCreatedSince(any(Instant.class))).thenReturn(3L);
        when(transactionService.countTransactions()).thenReturn(14L);
        when(transactionService.countTransactionsCreatedSince(any(Instant.class))).thenReturn(6L);
        when(notificationService.countNotifications()).thenReturn(20L);

        AdminDashboardResponse response = adminService.getDashboard();

        assertThat(response.totalUsers()).isEqualTo(10L);
        assertThat(response.activeUsers()).isEqualTo(8L);
        assertThat(response.blockedUsers()).isEqualTo(2L);
        assertThat(response.totalWallets()).isEqualTo(10L);
        assertThat(response.totalWalletBalance()).isEqualByComparingTo("1250.50");
        assertThat(response.todaysPayments()).isEqualTo(3L);
        assertThat(response.todaysTransactions()).isEqualTo(6L);
    }

    @Test
    void updateUserDelegatesToUserService() {
        UUID userId = UUID.randomUUID();
        AdminUserUpdateRequest request = new AdminUserUpdateRequest("Monika@Example.com", UserStatus.ACTIVE);
        UserAdminResponse user = new UserAdminResponse(userId, "monika@example.com", UserRole.USER, UserStatus.ACTIVE);
        when(userAccountService.updateUserForAdmin(userId, request.email(), request.status())).thenReturn(user);

        UserAdminResponse response = adminService.updateUser(userId, request);

        assertThat(response).isEqualTo(user);
    }

    @Test
    void updateUserRoleRejectsSelfRoleChange() {
        UUID currentAdminId = UUID.randomUUID();

        assertThatThrownBy(() -> adminService.updateUserRole(currentAdminId, currentAdminId, UserRole.USER))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateUserRoleDelegatesForOtherUser() {
        UUID currentAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UserAdminResponse user = new UserAdminResponse(targetUserId, "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
        when(userAccountService.updateUserRole(targetUserId, UserRole.ADMIN)).thenReturn(user);

        UserAdminResponse response = adminService.updateUserRole(currentAdminId, targetUserId, UserRole.ADMIN);

        assertThat(response).isEqualTo(user);
    }

    @Test
    void blockWalletDelegatesToWalletService() {
        UUID walletId = UUID.randomUUID();
        WalletResponse wallet = new WalletResponse(
                walletId,
                UUID.randomUUID(),
                BigDecimal.ZERO.setScale(2),
                "INR",
                WalletStatus.BLOCKED,
                Instant.now(),
                Instant.now()
        );
        when(walletService.blockWallet(walletId)).thenReturn(wallet);

        WalletResponse response = adminService.blockWallet(walletId);

        assertThat(response.status()).isEqualTo(WalletStatus.BLOCKED);
        verify(walletService).blockWallet(walletId);
    }
}
