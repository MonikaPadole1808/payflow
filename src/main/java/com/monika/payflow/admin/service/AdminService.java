package com.monika.payflow.admin.service;

import com.monika.payflow.admin.dto.AdminDashboardResponse;
import com.monika.payflow.admin.dto.AdminUserUpdateRequest;
import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.notification.service.NotificationService;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.payment.service.PaymentService;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.transaction.service.TransactionService;
import com.monika.payflow.user.dto.UserAdminResponse;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.service.UserAccountService;
import com.monika.payflow.wallet.dto.WalletResponse;
import com.monika.payflow.wallet.service.WalletService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final UserAccountService userAccountService;
    private final WalletService walletService;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    public AdminService(
            UserAccountService userAccountService,
            WalletService walletService,
            PaymentService paymentService,
            TransactionService transactionService,
            NotificationService notificationService
    ) {
        this.userAccountService = userAccountService;
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }

    public AdminDashboardResponse getDashboard() {
        Instant todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return new AdminDashboardResponse(
                userAccountService.countUsers(),
                userAccountService.countActiveUsers(),
                userAccountService.countBlockedUsers(),
                walletService.countWallets(),
                paymentService.countPayments(),
                transactionService.countTransactions(),
                notificationService.countNotifications(),
                walletService.getTotalWalletBalance(),
                paymentService.countPaymentsCreatedSince(todayStart),
                transactionService.countTransactionsCreatedSince(todayStart)
        );
    }

    public List<UserAdminResponse> getUsers(String search) {
        return userAccountService.getUsersForAdmin(search);
    }

    public UserAdminResponse getUser(UUID userId) {
        return userAccountService.getUserForAdmin(userId);
    }

    public UserAdminResponse updateUser(UUID userId, AdminUserUpdateRequest request) {
        return userAccountService.updateUserForAdmin(userId, request.email(), request.status());
    }

    public UserAdminResponse activateUser(UUID userId) {
        return userAccountService.activateUser(userId);
    }

    public UserAdminResponse deactivateUser(UUID userId) {
        return userAccountService.deactivateUser(userId);
    }

    public UserAdminResponse blockUser(UUID userId) {
        return userAccountService.deactivateUser(userId);
    }

    public UserAdminResponse unblockUser(UUID userId) {
        return userAccountService.activateUser(userId);
    }

    public UserRole getUserRole(UUID userId) {
        return userAccountService.getUserForAdmin(userId).role();
    }

    public UserAdminResponse updateUserRole(UUID currentAdminUserId, UUID targetUserId, UserRole role) {
        if (currentAdminUserId.equals(targetUserId)) {
            throw new BadRequestException(
                    ErrorCode.ADMIN_SELF_ROLE_CHANGE_NOT_ALLOWED.defaultMessage(),
                    ErrorCode.ADMIN_SELF_ROLE_CHANGE_NOT_ALLOWED
            );
        }

        return userAccountService.updateUserRole(targetUserId, role);
    }

    public List<WalletResponse> getWallets(String search) {
        return walletService.getWalletsForAdmin(search);
    }

    public WalletResponse getWallet(UUID walletId) {
        return walletService.getWalletForAdmin(walletId);
    }

    public WalletResponse blockWallet(UUID walletId) {
        return walletService.blockWallet(walletId);
    }

    public WalletResponse unblockWallet(UUID walletId) {
        return walletService.unblockWallet(walletId);
    }

    public List<PaymentResponse> getPayments(String search) {
        return paymentService.getPaymentsForAdmin(search);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        return paymentService.getPaymentForAdmin(paymentId);
    }

    public List<TransactionResponse> getTransactions(String search) {
        return transactionService.getTransactionsForAdmin(search);
    }

    public TransactionResponse getTransaction(UUID transactionId) {
        return transactionService.getTransactionForAdmin(transactionId);
    }

    public List<NotificationResponse> getNotifications(String search) {
        return notificationService.getNotificationsForAdmin(search);
    }

    public NotificationResponse getNotification(UUID notificationId) {
        return notificationService.getNotificationForAdmin(notificationId);
    }
}
