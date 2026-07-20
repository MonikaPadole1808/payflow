package com.monika.payflow.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "notification_type", length = 30)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, name = "reference_number", length = 64)
    private String referenceNumber;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
    }

    public Notification(
            UUID userId,
            NotificationType notificationType,
            NotificationStatus status,
            String title,
            String message,
            String referenceNumber
    ) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.status = status;
        this.title = title;
        this.message = message;
        this.referenceNumber = referenceNumber;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public NotificationType notificationType() {
        return notificationType;
    }

    public NotificationStatus status() {
        return status;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    public String referenceNumber() {
        return referenceNumber;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant readAt() {
        return readAt;
    }

    public void markAsRead() {
        if (status == NotificationStatus.READ) {
            return;
        }

        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }
}
