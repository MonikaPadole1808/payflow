package com.monika.payflow.wallet.service;

public record WalletTransferResult(
        WalletBalanceChange sender,
        WalletBalanceChange receiver
) {
}
