package com.monika.payflow.payment.dto;

import com.monika.payflow.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID senderWalletId,
        UUID receiverWalletId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String referenceNumber,
        String description,
        Instant createdAt
) {
}
