package com.hospitalmanagement.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdministratorAssignmentRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_]{3,30}$") String username
) {
}
