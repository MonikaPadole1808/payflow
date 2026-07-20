package com.monika.payflow.payment.service;

import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.payment.dto.PaymentResponse;
import com.monika.payflow.payment.dto.PaymentTransferRequest;
import com.monika.payflow.payment.entity.Payment;
import com.monika.payflow.payment.entity.PaymentStatus;
import com.monika.payflow.payment.repository.PaymentRepository;
import com.monika.payflow.transaction.service.TransactionRecorder;
import com.monika.payflow.wallet.service.WalletBalanceChange;
import com.monika.payflow.wallet.service.WalletTransferResult;
import com.monika.payflow.wallet.service.WalletTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final String REFERENCE_PREFIX = "PAY-";
    private static final String DEFAULT_DESCRIPTION = "Wallet transfer";

    private final PaymentRepository paymentRepository;
    private final WalletTransferService walletTransferService;
    private final TransactionRecorder transactionRecorder;

    public PaymentService(
            PaymentRepository paymentRepository,
            WalletTransferService walletTransferService,
            TransactionRecorder transactionRecorder
    ) {
        this.paymentRepository = paymentRepository;
        this.walletTransferService = walletTransferService;
        this.transactionRecorder = transactionRecorder;
    }

    @Transactional
    public PaymentResponse transfer(UUID senderUserId, PaymentTransferRequest request) {
        if (senderUserId.equals(request.receiverUserId())) {
            throw new BadRequestException(
                    ErrorCode.PAYMENT_SELF_TRANSFER_NOT_ALLOWED.defaultMessage(),
                    ErrorCode.PAYMENT_SELF_TRANSFER_NOT_ALLOWED
            );
        }

        WalletTransferResult transferResult = walletTransferService.transferBetweenUsers(
                senderUserId,
                request.receiverUserId(),
                request.amount()
        );

        WalletBalanceChange senderChange = transferResult.sender();
        WalletBalanceChange receiverChange = transferResult.receiver();
        Payment payment = paymentRepository.save(new Payment(
                senderChange.wallet(),
                receiverChange.wallet(),
                normalizeAmount(request.amount()),
                senderChange.wallet().currency(),
                PaymentStatus.SUCCESS,
                generateReferenceNumber(),
                resolveDescription(request.description())
        ));

        transactionRecorder.recordTransferOut(
                senderChange.wallet(),
                senderChange.amount(),
                senderChange.balanceBefore(),
                senderChange.balanceAfter()
        );
        transactionRecorder.recordTransferIn(
                receiverChange.wallet(),
                receiverChange.amount(),
                receiverChange.balanceBefore(),
                receiverChange.balanceAfter()
        );

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(UUID userId) {
        return paymentRepository.findUserPaymentsOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID userId, UUID paymentId) {
        Payment payment = paymentRepository.findUserPaymentById(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.PAYMENT_NOT_FOUND.defaultMessage(),
                        ErrorCode.PAYMENT_NOT_FOUND
                ));

        return toResponse(payment);
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

    private String resolveDescription(String description) {
        if (description == null || description.isBlank()) {
            return DEFAULT_DESCRIPTION;
        }

        return description.trim();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.id(),
                payment.senderWalletId(),
                payment.receiverWalletId(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.referenceNumber(),
                payment.description(),
                payment.createdAt()
        );
    }
}
