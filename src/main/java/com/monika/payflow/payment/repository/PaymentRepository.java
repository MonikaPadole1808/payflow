package com.monika.payflow.payment.repository;

import com.monika.payflow.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            select payment from Payment payment
            where payment.senderWallet.user.id = :userId
               or payment.receiverWallet.user.id = :userId
            order by payment.createdAt desc
            """)
    List<Payment> findUserPaymentsOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("""
            select payment from Payment payment
            where payment.id = :paymentId
              and (
                    payment.senderWallet.user.id = :userId
                 or payment.receiverWallet.user.id = :userId
              )
            """)
    Optional<Payment> findUserPaymentById(@Param("paymentId") UUID paymentId, @Param("userId") UUID userId);

    long countByCreatedAtGreaterThanEqual(Instant createdAt);
}
