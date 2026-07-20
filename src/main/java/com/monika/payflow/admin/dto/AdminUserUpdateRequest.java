package com.monika.payflow.admin.dto;

import com.monika.payflow.user.entity.UserStatus;
import jakarta.validation.constraints.Email;

public record AdminUserUpdateRequest(
        @Email(message = "email must be valid")
        String email,
        UserStatus status
) {
}
