package com.monika.payflow.notification.service;

import com.monika.payflow.common.exception.ResourceNotFoundException;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.notification.entity.Notification;
import com.monika.payflow.notification.entity.NotificationStatus;
import com.monika.payflow.notification.entity.NotificationType;
import com.monika.payflow.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void recordRegistrationCreatesUnreadRegistrationNotification() {
        UUID userId = UUID.randomUUID();

        notificationService.recordRegistration(userId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertThat(notification.userId()).isEqualTo(userId);
        assertThat(notification.notificationType()).isEqualTo(NotificationType.REGISTRATION);
        assertThat(notification.status()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notification.title()).isEqualTo("Registration completed");
        assertThat(notification.referenceNumber()).startsWith("NTF-");
    }

    @Test
    void recordDepositCreatesUnreadDepositNotification() {
        UUID userId = UUID.randomUUID();

        notificationService.recordDeposit(userId, new BigDecimal("25.25"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertThat(notification.notificationType()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(notification.message()).contains("25.25");
    }

    @Test
    void recordWithdrawalCreatesUnreadWithdrawalNotification() {
        UUID userId = UUID.randomUUID();

        notificationService.recordWithdrawal(userId, new BigDecimal("40.00"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertThat(notification.notificationType()).isEqualTo(NotificationType.WITHDRAW);
        assertThat(notification.message()).contains("40.00");
    }

    @Test
    void recordPaymentSentCreatesUnreadPaymentSentNotification() {
        UUID userId = UUID.randomUUID();

        notificationService.recordPaymentSent(userId, new BigDecimal("30.00"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().notificationType()).isEqualTo(NotificationType.PAYMENT_SENT);
    }

    @Test
    void recordPaymentReceivedCreatesUnreadPaymentReceivedNotification() {
        UUID userId = UUID.randomUUID();

        notificationService.recordPaymentReceived(userId, new BigDecimal("30.00"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().notificationType()).isEqualTo(NotificationType.PAYMENT_RECEIVED);
    }

    @Test
    void getNotificationHistoryReturnsRepositoryResult() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId, NotificationType.DEPOSIT, NotificationStatus.UNREAD);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notification));

        List<NotificationResponse> response = notificationService.getNotificationHistory(userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(notification.id());
        assertThat(response.getFirst().status()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    void getNotificationRejectsNotificationOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotification(userId, notificationId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAsReadChangesUnreadNotificationToRead() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId, NotificationType.DEPOSIT, NotificationStatus.UNREAD);
        when(notificationRepository.findByIdAndUserId(notification.id(), userId)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(userId, notification.id());

        assertThat(response.status()).isEqualTo(NotificationStatus.READ);
        assertThat(response.readAt()).isNotNull();
    }

    @Test
    void markAsReadIsIdempotentForReadNotifications() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId, NotificationType.DEPOSIT, NotificationStatus.READ);
        Instant readAt = Instant.now().minusSeconds(60);
        ReflectionTestUtils.setField(notification, "readAt", readAt);
        when(notificationRepository.findByIdAndUserId(notification.id(), userId)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(userId, notification.id());

        assertThat(response.status()).isEqualTo(NotificationStatus.READ);
        assertThat(response.readAt()).isEqualTo(readAt);
    }

    private Notification notification(UUID userId, NotificationType notificationType, NotificationStatus status) {
        Notification notification = new Notification(
                userId,
                notificationType,
                status,
                "Title",
                "Message",
                "NTF-" + UUID.randomUUID()
        );
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(notification, "createdAt", Instant.now());
        return notification;
    }
}
