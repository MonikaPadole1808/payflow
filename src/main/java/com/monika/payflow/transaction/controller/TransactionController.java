package com.monika.payflow.transaction.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.common.constants.AppConstants;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(
            @AuthenticationPrincipal AuthUserDetails currentUser
    ) {
        List<TransactionResponse> response = transactionService.getTransactionHistory(currentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @PathVariable UUID id
    ) {
        TransactionResponse response = transactionService.getTransaction(currentUser.id(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }
}
