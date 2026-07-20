package com.monika.payflow.wallet.service;

import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.BadRequestException;
import com.monika.payflow.common.exception.ConflictException;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.notification.service.NotificationRecorder;
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
    private final NotificationRecorder notificationRecorder;

    public WalletService(
            WalletRepository walletRepository,
            TransactionRecorder transactionRecorder,
            NotificationRecorder notificationRecorder
    ) {
        this.walletRepository = walletRepository;
        this.transactionRecorder = transactionRecorder;
        this.notificationRecorder = notificationRecorder;
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

    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsForAdmin(String search) {
        return walletRepository.findAll()
                .stream()
                .filter(wallet -> matchesSearch(wallet, search))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletForAdmin(UUID walletId) {
        return toResponse(findWalletById(walletId));
    }

    @Transactional
    public WalletResponse deposit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletForUpdate(userId);
        validateWalletIsActive(wallet);
        BigDecimal normalizedAmount = normalizeAmount(amount);
        BigDecimal balanceBefore = wallet.balance();
        BigDecimal balanceAfter = balanceBefore.add(normalizedAmount);

        wallet.updateBalance(balanceAfter);
        transactionRecorder.recordDeposit(wallet, normalizedAmount, balanceBefore, balanceAfter);
        notificationRecorder.recordDeposit(userId, normalizedAmount);

        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletForUpdate(userId);
        validateWalletIsActive(wallet);
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
        notificationRecorder.recordWithdrawal(userId, normalizedAmount);

        return toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletTransferResult transferBetweenUsers(UUID senderUserId, UUID receiverUserId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        Map<UUID, Wallet> walletsByUserId = findWalletsForUpdate(senderUserId, receiverUserId);
        Wallet senderWallet = walletsByUserId.get(senderUserId);
        Wallet receiverWallet = walletsByUserId.get(receiverUserId);
        validateWalletIsActive(senderWallet);
        validateWalletIsActive(receiverWallet);

        WalletBalanceChange senderChange = debit(senderWallet, normalizedAmount);
        WalletBalanceChange receiverChange = credit(receiverWallet, normalizedAmount);

        return new WalletTransferResult(senderChange, receiverChange);
    }

    @Transactional
    public WalletResponse blockWallet(UUID walletId) {
        Wallet wallet = findWalletById(walletId);
        wallet.updateStatus(WalletStatus.BLOCKED);
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse unblockWallet(UUID walletId) {
        Wallet wallet = findWalletById(walletId);
        wallet.updateStatus(WalletStatus.ACTIVE);
        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public long countWallets() {
        return walletRepository.count();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalWalletBalance() {
        return walletRepository.sumWalletBalances().setScale(2);
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

    private Wallet findWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
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
        validateWalletIsActive(wallet);
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
        validateWalletIsActive(wallet);
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

    private void validateWalletIsActive(Wallet wallet) {
        if (wallet.status() == WalletStatus.BLOCKED) {
            throw new BadRequestException(
                    ErrorCode.WALLET_BLOCKED.defaultMessage(),
                    ErrorCode.WALLET_BLOCKED
            );
        }
    }

    private boolean matchesSearch(Wallet wallet, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = search.trim().toLowerCase();
        return wallet.id().toString().contains(normalizedSearch)
                || wallet.userId().toString().contains(normalizedSearch)
                || wallet.currency().toLowerCase().contains(normalizedSearch)
                || wallet.status().name().toLowerCase().contains(normalizedSearch);
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
