package com.monika.payflow.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentTransferRequest(
        @NotNull(message = "receiverUserId is required")
        UUID receiverUserId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have up to 17 integer digits and 2 decimal places")
        BigDecimal amount,

        @Size(max = 255, message = "description must not exceed 255 characters")
        String description
) {
}
