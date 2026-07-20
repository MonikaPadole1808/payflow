package com.monika.payflow.wallet.service;

import com.monika.payflow.wallet.entity.Wallet;

import java.math.BigDecimal;

public record WalletBalanceChange(
        Wallet wallet,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
) {
}
