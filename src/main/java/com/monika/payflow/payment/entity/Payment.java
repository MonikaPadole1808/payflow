package com.monika.payflow.payment.entity;

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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, name = "reference_number", length = 64, unique = true)
    private String referenceNumber;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public Payment(
            Wallet senderWallet,
            Wallet receiverWallet,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String referenceNumber,
            String description
    ) {
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
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

    public UUID senderWalletId() {
        return senderWallet.id();
    }

    public UUID receiverWalletId() {
        return receiverWallet.id();
    }

    public UUID senderUserId() {
        return senderWallet.userId();
    }

    public UUID receiverUserId() {
        return receiverWallet.userId();
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public PaymentStatus status() {
        return status;
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
