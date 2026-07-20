package com.monika.payflow.payment.repository;

import com.monika.payflow.payment.entity.Payment;
import com.monika.payflow.payment.entity.PaymentStatus;
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
class PaymentRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void findUserPaymentsOrderByCreatedAtDescReturnsSenderAndReceiverPaymentsOnly() {
        Wallet firstWallet = wallet("first@example.com");
        Wallet secondWallet = wallet("second@example.com");
        Wallet thirdWallet = wallet("third@example.com");
        Payment sentPayment = paymentRepository.save(payment(firstWallet, secondWallet));
        Payment receivedPayment = paymentRepository.save(payment(thirdWallet, firstWallet));
        paymentRepository.save(payment(secondWallet, thirdWallet));

        List<Payment> payments = paymentRepository.findUserPaymentsOrderByCreatedAtDesc(firstWallet.userId());

        assertThat(payments)
                .extracting(Payment::id)
                .containsExactly(receivedPayment.id(), sentPayment.id());
    }

    @Test
    void findUserPaymentByIdRejectsPaymentsNotOwnedByUser() {
        Wallet senderWallet = wallet("sender@example.com");
        Wallet receiverWallet = wallet("receiver@example.com");
        Wallet otherWallet = wallet("other@example.com");
        Payment payment = paymentRepository.save(payment(senderWallet, receiverWallet));

        assertThat(paymentRepository.findUserPaymentById(payment.id(), otherWallet.userId()))
                .isEmpty();
    }

    private Wallet wallet(String email) {
        User user = userRepository.save(new User(email, "encoded-password", UserRole.USER, UserStatus.ACTIVE));
        return walletRepository.save(new Wallet(user, BigDecimal.ZERO.setScale(2), "INR", WalletStatus.ACTIVE));
    }

    private Payment payment(Wallet senderWallet, Wallet receiverWallet) {
        return new Payment(
                senderWallet,
                receiverWallet,
                new BigDecimal("30.00"),
                "INR",
                PaymentStatus.SUCCESS,
                "PAY-" + UUID.randomUUID(),
                "Wallet transfer"
        );
    }
}
