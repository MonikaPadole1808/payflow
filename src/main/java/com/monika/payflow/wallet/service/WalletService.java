package com.monika.payflow.wallet.service;

import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.transaction.service.TransactionRecorder;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.wallet.dto.WalletResponse;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import com.monika.payflow.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletService implements WalletProvisioningService, WalletTransferService {

    private static final BigDecimal ZERO_BALANCE = BigDecimal.ZERO.setScale(2);
    private static final String DEFAULT_CURRENCY = "INR";

    private final WalletRepository walletRepository;
    private final TransactionRecorder transactionRecorder;

    public WalletService(WalletRepository walletRepository, TransactionRecorder transactionRecorder) {
        this.walletRepository = walletRepository;
        this.transactionRecorder = transactionRecorder;
    }

    @Override
    @Transactional
    public void createWalletForUser(User user) {
        if (walletRepository.existsByUserId(user.id())) {
            throw new ConflictException(
                    ErrorCode.WALLET_ALREADY_EXISTS.defaultMessage(),
                    ErrorCode.WALLET_ALREADY_EXISTS
            );
        }

        Wallet wallet = new Wallet(user, ZERO_BALANCE, DEFAULT_CURRENCY, WalletStatus.ACTIVE);
        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        Wallet wallet = findWallet(userId);
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletForUpdate(userId);
        BigDecimal normalizedAmount = normalizeAmount(amount);
        BigDecimal balanceBefore = wallet.balance();
        BigDecimal balanceAfter = balanceBefore.add(normalizedAmount);

        wallet.updateBalance(balanceAfter);
        transactionRecorder.recordDeposit(wallet, normalizedAmount, balanceBefore, balanceAfter);

        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletForUpdate(userId);
        BigDecimal normalizedAmount = normalizeAmount(amount);
        BigDecimal balanceBefore = wallet.balance();

        if (balanceBefore.compareTo(normalizedAmount) < 0) {
            throw new BadRequestException(
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE.defaultMessage(),
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE
            );
        }

        BigDecimal balanceAfter = balanceBefore.subtract(normalizedAmount);
        wallet.updateBalance(balanceAfter);
        transactionRecorder.recordWithdrawal(wallet, normalizedAmount, balanceBefore, balanceAfter);

        return toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletTransferResult transferBetweenUsers(UUID senderUserId, UUID receiverUserId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        Map<UUID, Wallet> walletsByUserId = findWalletsForUpdate(senderUserId, receiverUserId);
        Wallet senderWallet = walletsByUserId.get(senderUserId);
        Wallet receiverWallet = walletsByUserId.get(receiverUserId);

        WalletBalanceChange senderChange = debit(senderWallet, normalizedAmount);
        WalletBalanceChange receiverChange = credit(receiverWallet, normalizedAmount);

        return new WalletTransferResult(senderChange, receiverChange);
    }

    private Wallet findWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WALLET_NOT_FOUND.defaultMessage(),
                        ErrorCode.WALLET_NOT_FOUND
                ));
    }

    private Wallet findWalletForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.WALLET_NOT_FOUND.defaultMessage(),
                        ErrorCode.WALLET_NOT_FOUND
                ));
    }

    private Map<UUID, Wallet> findWalletsForUpdate(UUID senderUserId, UUID receiverUserId) {
        List<Wallet> wallets = walletRepository.findByUserIdInForUpdate(List.of(senderUserId, receiverUserId));
        Map<UUID, Wallet> walletsByUserId = wallets.stream()
                .collect(Collectors.toMap(Wallet::userId, wallet -> wallet));

        if (!walletsByUserId.containsKey(senderUserId) || !walletsByUserId.containsKey(receiverUserId)) {
            throw new ResourceNotFoundException(
                    ErrorCode.WALLET_NOT_FOUND.defaultMessage(),
                    ErrorCode.WALLET_NOT_FOUND
            );
        }

        return walletsByUserId;
    }

    private WalletBalanceChange debit(Wallet wallet, BigDecimal amount) {
        BigDecimal balanceBefore = wallet.balance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new BadRequestException(
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE.defaultMessage(),
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE
            );
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.updateBalance(balanceAfter);
        return new WalletBalanceChange(wallet, amount, balanceBefore, balanceAfter);
    }

    private WalletBalanceChange credit(Wallet wallet, BigDecimal amount) {
        BigDecimal balanceBefore = wallet.balance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        wallet.updateBalance(balanceAfter);
        return new WalletBalanceChange(wallet, amount, balanceBefore, balanceAfter);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.defaultMessage());
        }

        return amount.setScale(2);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.id(),
                wallet.userId(),
                wallet.balance(),
                wallet.currency(),
                wallet.status(),
                wallet.createdAt(),
                wallet.updatedAt()
        );
    }
}
