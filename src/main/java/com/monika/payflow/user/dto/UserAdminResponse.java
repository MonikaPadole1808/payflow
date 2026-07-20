package com.monika.payflow.user.dto;

import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;

import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status
) {
}
