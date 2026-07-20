package com.monika.payflow.transaction.repository;

import com.monika.payflow.transaction.entity.Transaction;
import com.monika.payflow.transaction.entity.TransactionStatus;
import com.monika.payflow.transaction.entity.TransactionType;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.repository.UserRepository;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import com.monika.payflow.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void findByWalletUserIdOrderByCreatedAtDescReturnsOnlyUserTransactions() {
        Wallet firstWallet = wallet("first-owner@example.com");
        Wallet secondWallet = wallet("second-owner@example.com");
        Transaction olderTransaction = transactionRepository.save(transaction(firstWallet, "TXN-" + UUID.randomUUID()));
        Transaction newerTransaction = transactionRepository.save(transaction(firstWallet, "TXN-" + UUID.randomUUID()));
        transactionRepository.save(transaction(secondWallet, "TXN-" + UUID.randomUUID()));

        List<Transaction> transactions = transactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(firstWallet.userId());

        assertThat(transactions)
                .extracting(Transaction::id)
                .containsExactly(newerTransaction.id(), olderTransaction.id());
    }

    @Test
    void findByIdAndWalletUserIdRejectsTransactionsOwnedByAnotherUser() {
        Wallet ownerWallet = wallet("owner@example.com");
        Wallet otherWallet = wallet("other-owner@example.com");
        Transaction transaction = transactionRepository.save(transaction(ownerWallet, "TXN-" + UUID.randomUUID()));

        assertThat(transactionRepository.findByIdAndWallet_User_Id(transaction.id(), otherWallet.userId()))
                .isEmpty();
    }

    private Wallet wallet(String email) {
        User user = userRepository.save(new User(email, "encoded-password", UserRole.USER, UserStatus.ACTIVE));
        return walletRepository.save(new Wallet(user, BigDecimal.ZERO.setScale(2), "INR", WalletStatus.ACTIVE));
    }

    private Transaction transaction(Wallet wallet, String referenceNumber) {
        return new Transaction(
                wallet,
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                new BigDecimal("25.00"),
                new BigDecimal("100.00"),
                new BigDecimal("125.00"),
                "INR",
                referenceNumber,
                "Wallet deposit"
        );
    }
}
