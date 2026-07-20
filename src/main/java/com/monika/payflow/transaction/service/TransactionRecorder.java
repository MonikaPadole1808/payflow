package com.monika.payflow.transaction.service;

import com.monika.payflow.wallet.entity.Wallet;

import java.math.BigDecimal;

public interface TransactionRecorder {

    void recordDeposit(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter);

    void recordWithdrawal(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter);
}
