package com.monika.payflow.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalUsers,
        long activeUsers,
        long blockedUsers,
        long totalWallets,
        long totalPayments,
        long totalTransactions,
        long totalNotifications,
        BigDecimal totalWalletBalance,
        long todaysPayments,
        long todaysTransactions
) {
}
