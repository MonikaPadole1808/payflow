package com.monika.payflow.wallet.repository;

import com.monika.payflow.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from Wallet wallet where wallet.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wallet from Wallet wallet where wallet.user.id in :userIds order by wallet.id")
    List<Wallet> findByUserIdInForUpdate(@Param("userIds") Collection<UUID> userIds);
}
