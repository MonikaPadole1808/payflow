package com.monika.payflow.wallet.repository;

import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.repository.UserRepository;
import com.monika.payflow.wallet.entity.Wallet;
import com.monika.payflow.wallet.entity.WalletStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WalletRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void findByUserIdReturnsUserWallet() {
        User user = userRepository.save(new User(
                "monika@example.com",
                "encoded-password",
                UserRole.USER,
                UserStatus.ACTIVE
        ));
        Wallet wallet = walletRepository.save(new Wallet(
                user,
                new BigDecimal("50.00"),
                "INR",
                WalletStatus.ACTIVE
        ));

        assertThat(walletRepository.findByUserId(user.id()))
                .isPresent()
                .get()
                .extracting(Wallet::id)
                .isEqualTo(wallet.id());
    }

    @Test
    void existsByUserIdReturnsTrueWhenWalletExists() {
        User user = userRepository.save(new User(
                "wallet-owner@example.com",
                "encoded-password",
                UserRole.USER,
                UserStatus.ACTIVE
        ));
        walletRepository.save(new Wallet(user, BigDecimal.ZERO.setScale(2), "INR", WalletStatus.ACTIVE));

        assertThat(walletRepository.existsByUserId(user.id())).isTrue();
    }
}
