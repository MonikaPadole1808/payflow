package com.monika.payflow.wallet.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.common.constants.AppConstants;
import com.monika.payflow.wallet.dto.WalletAmountRequest;
import com.monika.payflow.wallet.dto.WalletResponse;
import com.monika.payflow.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@AuthenticationPrincipal AuthUserDetails currentUser) {
        WalletResponse response = walletService.getWallet(currentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully", response));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        WalletResponse response = walletService.deposit(currentUser.id(), request.amount());
        return ResponseEntity.ok(ApiResponse.success("Wallet deposit completed successfully", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletResponse>> withdraw(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        WalletResponse response = walletService.withdraw(currentUser.id(), request.amount());
        return ResponseEntity.ok(ApiResponse.success("Wallet withdrawal completed successfully", response));
    }
}
