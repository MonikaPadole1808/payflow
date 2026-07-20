package com.monika.payflow.admin.controller;

import com.monika.payflow.admin.dto.AdminDashboardResponse;
import com.monika.payflow.admin.dto.AdminRoleUpdateRequest;
import com.monika.payflow.admin.service.AdminService;
import com.monika.payflow.auth.security.AuthUserDetails;
import com.monika.payflow.common.api.ApiResponse;
import com.monika.payflow.user.dto.UserAdminResponse;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getDashboardReturnsStandardApiResponse() {
        AdminDashboardResponse dashboard = new AdminDashboardResponse(
                10L,
                8L,
                2L,
                10L,
                7L,
                14L,
                20L,
                new BigDecimal("1250.50"),
                3L,
                6L
        );
        when(adminService.getDashboard()).thenReturn(dashboard);

        ResponseEntity<ApiResponse<AdminDashboardResponse>> response = adminController.getDashboard();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(dashboard);
    }

    @Test
    void updateUserRoleReturnsStandardApiResponse() {
        UUID currentAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        AuthUserDetails currentUser = currentUser(currentAdminId);
        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest(UserRole.ADMIN);
        UserAdminResponse user = new UserAdminResponse(targetUserId, "admin@example.com", UserRole.ADMIN, UserStatus.ACTIVE);
        when(adminService.updateUserRole(currentAdminId, targetUserId, UserRole.ADMIN)).thenReturn(user);

        ResponseEntity<ApiResponse<UserAdminResponse>> response =
                adminController.updateUserRole(currentUser, targetUserId, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Admin user role updated successfully");
        assertThat(response.getBody().data()).isEqualTo(user);
    }

    private AuthUserDetails currentUser(UUID userId) {
        User user = new User("admin@example.com", "encoded-password", UserRole.ADMIN, UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "id", userId);
        return new AuthUserDetails(user);
    }
}
