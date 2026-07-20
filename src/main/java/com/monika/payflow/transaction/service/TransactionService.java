package com.monika.payflow.transaction.service;

import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.transaction.dto.TransactionResponse;
import com.monika.payflow.transaction.entity.Transaction;
import com.monika.payflow.transaction.entity.TransactionStatus;
import com.monika.payflow.transaction.entity.TransactionType;
import com.monika.payflow.transaction.repository.TransactionRepository;
import com.monika.payflow.wallet.entity.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements TransactionRecorder {

    private static final String REFERENCE_PREFIX = "TXN-";
    private static final String DEPOSIT_DESCRIPTION = "Wallet deposit";
    private static final String WITHDRAWAL_DESCRIPTION = "Wallet withdrawal";
    private static final String TRANSFER_OUT_DESCRIPTION = "Wallet transfer sent";
    private static final String TRANSFER_IN_DESCRIPTION = "Wallet transfer received";

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void recordDeposit(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        record(wallet, TransactionType.DEPOSIT, amount, balanceBefore, balanceAfter, DEPOSIT_DESCRIPTION);
    }

    @Override
    public void recordWithdrawal(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        record(wallet, TransactionType.WITHDRAW, amount, balanceBefore, balanceAfter, WITHDRAWAL_DESCRIPTION);
    }

    @Override
    public void recordTransferOut(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        record(wallet, TransactionType.TRANSFER_OUT, amount, balanceBefore, balanceAfter, TRANSFER_OUT_DESCRIPTION);
    }

    @Override
    public void recordTransferIn(Wallet wallet, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        record(wallet, TransactionType.TRANSFER_IN, amount, balanceBefore, balanceAfter, TRANSFER_IN_DESCRIPTION);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(UUID userId) {
        return transactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndWallet_User_Id(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND.defaultMessage(),
                        ErrorCode.TRANSACTION_NOT_FOUND
                ));

        return toResponse(transaction);
    }

    private void record(
            Wallet wallet,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description
    ) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        Transaction transaction = new Transaction(
                wallet,
                transactionType,
                TransactionStatus.SUCCESS,
                normalizedAmount,
                balanceBefore.setScale(2),
                balanceAfter.setScale(2),
                wallet.currency(),
                generateReferenceNumber(),
                description
        );

        transactionRepository.save(transaction);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.defaultMessage());
        }

        return amount.setScale(2);
    }

    private String generateReferenceNumber() {
        return REFERENCE_PREFIX + UUID.randomUUID();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.walletId(),
                transaction.transactionType(),
                transaction.status(),
                transaction.amount(),
                transaction.balanceBefore(),
                transaction.balanceAfter(),
                transaction.currency(),
                transaction.referenceNumber(),
                transaction.description(),
                transaction.createdAt()
        );
    }
}
