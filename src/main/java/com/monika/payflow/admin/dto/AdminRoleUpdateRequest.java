package com.monika.payflow.admin.dto;

import com.monika.payflow.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminRoleUpdateRequest(
        @NotNull(message = "role is required")
        UserRole role
) {
}
