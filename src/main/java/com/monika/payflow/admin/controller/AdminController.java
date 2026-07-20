package com.monika.payflow.admin.controller;

import com.monika.payflow.admin.dto.AdminDashboardResponse;
import com.monika.payflow.admin.dto.AdminRoleUpdateRequest;
import com.monika.payflow.admin.dto.AdminUserUpdateRequest;
import com.monika.payflow.admin.service.AdminService;
import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.common.constants.AppConstants;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.user.dto.UserAdminResponse;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.wallet.dto.WalletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard retrieved successfully", adminService.getDashboard()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getUsers(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin users retrieved successfully", adminService.getUsers(search)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user retrieved successfully", adminService.getUser(id)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Admin user updated successfully", adminService.updateUser(id, request)));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserAdminResponse>> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user activated successfully", adminService.activateUser(id)));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserAdminResponse>> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user deactivated successfully", adminService.deactivateUser(id)));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<UserAdminResponse>> blockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user blocked successfully", adminService.blockUser(id)));
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse<UserAdminResponse>> unblockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user unblocked successfully", adminService.unblockUser(id)));
    }

    @GetMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserRole>> getUserRole(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin user role retrieved successfully", adminService.getUserRole(id)));
    }

    @PatchMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateUserRole(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody AdminRoleUpdateRequest request
    ) {
        UserAdminResponse response = adminService.updateUserRole(currentUser.id(), id, request.role());
        return ResponseEntity.ok(ApiResponse.success("Admin user role updated successfully", response));
    }

    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getWallets(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin wallets retrieved successfully", adminService.getWallets(search)));
    }

    @GetMapping("/wallets/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin wallet retrieved successfully", adminService.getWallet(id)));
    }

    @PatchMapping("/wallets/{id}/block")
    public ResponseEntity<ApiResponse<WalletResponse>> blockWallet(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin wallet blocked successfully", adminService.blockWallet(id)));
    }

    @PatchMapping("/wallets/{id}/unblock")
    public ResponseEntity<ApiResponse<WalletResponse>> unblockWallet(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin wallet unblocked successfully", adminService.unblockWallet(id)));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin payments retrieved successfully", adminService.getPayments(search)));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin payment retrieved successfully", adminService.getPayment(id)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin transactions retrieved successfully", adminService.getTransactions(search)));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin transaction retrieved successfully", adminService.getTransaction(id)));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success("Admin notifications retrieved successfully", adminService.getNotifications(search)));
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Admin notification retrieved successfully", adminService.getNotification(id)));
    }
}
