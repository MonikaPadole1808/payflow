package com.monika.payflow.transaction.repository;

import com.monika.payflow.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWallet_User_IdOrderByCreatedAtDesc(UUID userId);

    Optional<Transaction> findByIdAndWallet_User_Id(UUID id, UUID userId);
}
