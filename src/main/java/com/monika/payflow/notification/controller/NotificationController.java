package com.monika.payflow.notification.controller;

import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.common.constants.AppConstants;
import com.monika.payflow.notification.dto.NotificationResponse;
import com.monika.payflow.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationHistory(
            @AuthenticationPrincipal AuthUserDetails currentUser
    ) {
        List<NotificationResponse> response = notificationService.getNotificationHistory(currentUser.id());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @PathVariable UUID id
    ) {
        NotificationResponse response = notificationService.getNotification(currentUser.id(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", response));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal AuthUserDetails currentUser,
            @PathVariable UUID id
    ) {
        NotificationResponse response = notificationService.markAsRead(currentUser.id(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully", response));
    }
}
