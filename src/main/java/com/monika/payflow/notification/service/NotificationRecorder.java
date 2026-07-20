package com.monika.payflow.notification.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface NotificationRecorder {

    void recordRegistration(UUID userId);

    void recordDeposit(UUID userId, BigDecimal amount);

    void recordWithdrawal(UUID userId, BigDecimal amount);

    void recordPaymentSent(UUID userId, BigDecimal amount);

    void recordPaymentReceived(UUID userId, BigDecimal amount);
}
