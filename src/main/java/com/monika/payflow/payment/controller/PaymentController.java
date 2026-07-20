package com.monika.payflow.payment.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.common.constants.AppConstants;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.payment.dto.PaymentTransferRequest;
import com.monika.payflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<PaymentResponse>> transfer(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @Valid @RequestBody PaymentTransferRequest request
    ) {
        PaymentResponse response = paymentService.transfer(currentUser.id(), request);
        return ResponseEntity.ok(ApiResponse.success("Payment transfer completed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @AuthenticationPrincipal AuthUserDetails currentUser
    ) {
        List<PaymentResponse> response = paymentService.getPaymentHistory(currentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @PathVariable UUID id
    ) {
        PaymentResponse response = paymentService.getPayment(currentUser.id(), id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }
}
