package com.monika.payflow.transaction.dto;

import com.monika.payflow.transaction.entity.TransactionStatus;
import com.monika.payflow.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID walletId,
        TransactionType transactionType,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String currency,
        String referenceNumber,
        String description,
        Instant createdAt
) {
}
