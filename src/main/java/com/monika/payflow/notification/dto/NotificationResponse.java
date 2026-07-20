package com.monika.payflow.notification.dto;

import com.monika.payflow.notification.entity.NotificationStatus;
import com.monika.payflow.notification.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType notificationType,
        NotificationStatus status,
        String title,
        String message,
        String referenceNumber,
        Instant createdAt,
        Instant readAt
) {
}
