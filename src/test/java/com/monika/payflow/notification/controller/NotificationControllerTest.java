package com.monika.payflow.notification.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.notification.entity.NotificationStatus;
import com.monika.payflow.notification.entity.NotificationType;
import com.monika.payflow.notification.service.NotificationService;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void getNotificationHistoryReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        NotificationResponse notification = notificationResponse(UUID.randomUUID(), NotificationStatus.UNREAD);
        when(notificationService.getNotificationHistory(userId)).thenReturn(List.of(notification));

        ResponseEntity<ApiResponse<List<NotificationResponse>>> response =
                notificationController.getNotificationHistory(currentUser);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Notifications retrieved successfully");
        assertThat(response.getBody().data()).containsExactly(notification);
    }

    @Test
    void getNotificationReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        NotificationResponse notification = notificationResponse(notificationId, NotificationStatus.UNREAD);
        when(notificationService.getNotification(userId, notificationId)).thenReturn(notification);

        ResponseEntity<ApiResponse<NotificationResponse>> response =
                notificationController.getNotification(currentUser, notificationId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Notification retrieved successfully");
        assertThat(response.getBody().data()).isEqualTo(notification);
    }

    @Test
    void markAsReadReturnsStandardApiResponse() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(userId);
        NotificationResponse notification = notificationResponse(notificationId, NotificationStatus.READ);
        when(notificationService.markAsRead(userId, notificationId)).thenReturn(notification);

        ResponseEntity<ApiResponse<NotificationResponse>> response =
                notificationController.markAsRead(currentUser, notificationId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Notification marked as read successfully");
        assertThat(response.getBody().data()).isEqualTo(notification);
    }

    private AuthUserDetails currentUser(UUID userId) {
        User user = new User("monika@example.com", "encoded-password", UserRole.USER, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return new AuthUserDetails(user);
    }

    private NotificationResponse notificationResponse(UUID notificationId, NotificationStatus status) {
        Instant readAt = status == NotificationStatus.READ ? Instant.now() : null;
        return new NotificationResponse(
                notificationId,
                UUID.randomUUID(),
                NotificationType.DEPOSIT,
                status,
                "Deposit successful",
                "Your wallet deposit was completed successfully.",
                "NTF-" + UUID.randomUUID(),
                Instant.now(),
                readAt
        );
    }
}
