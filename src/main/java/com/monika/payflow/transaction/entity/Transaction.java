package com.monika.payflow.transaction.entity;

import com.monika.payflow.wallet.entity.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transaction_type", length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, name = "balance_before", precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, name = "reference_number", length = 64, unique = true)
    private String referenceNumber;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(
            Wallet wallet,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String currency,
            String referenceNumber,
            String description
    ) {
        this.wallet = wallet;
        this.transactionType = transactionType;
        this.status = status;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.currency = currency;
        this.referenceNumber = referenceNumber;
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID walletId() {
        return wallet.id();
    }

    public UUID userId() {
        return wallet.userId();
    }

    public TransactionType transactionType() {
        return transactionType;
    }

    public TransactionStatus status() {
        return status;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal balanceBefore() {
        return balanceBefore;
    }

    public BigDecimal balanceAfter() {
        return balanceAfter;
    }

    public String currency() {
        return currency;
    }

    public String referenceNumber() {
        return referenceNumber;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
