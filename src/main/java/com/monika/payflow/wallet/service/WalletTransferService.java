package com.monika.payflow.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletTransferService {

    WalletTransferResult transferBetweenUsers(UUID senderUserId, UUID receiverUserId, BigDecimal amount);
}
