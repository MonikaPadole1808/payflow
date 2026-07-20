package com.monika.payflow.notification.service;

import com.monika.payflow.common.error.ErrorCode;
import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.notification.entity.Notification;
import com.monika.payflow.notification.entity.NotificationStatus;
import com.monika.payflow.notification.entity.NotificationType;
import com.monika.payflow.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements NotificationRecorder {

    private static final String REFERENCE_PREFIX = "NTF-";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void recordRegistration(UUID userId) {
        createUnread(
                userId,
                NotificationType.REGISTRATION,
                "Registration completed",
                "Your PayFlow account has been created successfully."
        );
    }

    @Override
    public void recordDeposit(UUID userId, BigDecimal amount) {
        createUnread(
                userId,
                NotificationType.DEPOSIT,
                "Deposit successful",
                "Your wallet deposit of %s was completed successfully.".formatted(formatAmount(amount))
        );
    }

    @Override
    public void recordWithdrawal(UUID userId, BigDecimal amount) {
        createUnread(
                userId,
                NotificationType.WITHDRAW,
                "Withdrawal successful",
                "Your wallet withdrawal of %s was completed successfully.".formatted(formatAmount(amount))
        );
    }

    @Override
    public void recordPaymentSent(UUID userId, BigDecimal amount) {
        createUnread(
                userId,
                NotificationType.PAYMENT_SENT,
                "Payment sent",
                "Your payment of %s was sent successfully.".formatted(formatAmount(amount))
        );
    }

    @Override
    public void recordPaymentReceived(UUID userId, BigDecimal amount) {
        createUnread(
                userId,
                NotificationType.PAYMENT_RECEIVED,
                "Payment received",
                "You received a payment of %s.".formatted(formatAmount(amount))
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationHistory(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID userId, UUID notificationId) {
        Notification notification = findOwnedNotification(userId, notificationId);
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = findOwnedNotification(userId, notificationId);
        notification.markAsRead();
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForAdmin(String search) {
        return notificationRepository.findAll()
                .stream()
                .filter(notification -> matchesSearch(notification, search))
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationForAdmin(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.NOTIFICATION_NOT_FOUND.defaultMessage(),
                        ErrorCode.NOTIFICATION_NOT_FOUND
                ));
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public long countNotifications() {
        return notificationRepository.count();
    }

    private void createUnread(UUID userId, NotificationType notificationType, String title, String message) {
        Notification notification = new Notification(
                userId,
                notificationType,
                NotificationStatus.UNREAD,
                title,
                message,
                generateReferenceNumber()
        );

        notificationRepository.save(notification);
    }

    private Notification findOwnedNotification(UUID userId, UUID notificationId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.NOTIFICATION_NOT_FOUND.defaultMessage(),
                        ErrorCode.NOTIFICATION_NOT_FOUND
                ));
    }

    private String generateReferenceNumber() {
        return REFERENCE_PREFIX + UUID.randomUUID();
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2).toPlainString();
    }

    private boolean matchesSearch(Notification notification, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String normalizedSearch = search.trim().toLowerCase();
        return notification.id().toString().contains(normalizedSearch)
                || notification.userId().toString().contains(normalizedSearch)
                || notification.notificationType().name().toLowerCase().contains(normalizedSearch)
                || notification.status().name().toLowerCase().contains(normalizedSearch)
                || notification.title().toLowerCase().contains(normalizedSearch)
                || notification.message().toLowerCase().contains(normalizedSearch)
                || notification.referenceNumber().toLowerCase().contains(normalizedSearch);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.userId(),
                notification.notificationType(),
                notification.status(),
                notification.title(),
                notification.message(),
                notification.referenceNumber(),
                notification.createdAt(),
                notification.readAt()
        );
    }
}
