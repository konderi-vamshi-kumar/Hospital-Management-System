package com.hospitalmanagement.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StaffInvitationCreateRequest(
        @NotBlank @Pattern(regexp = "HOSPITAL_ADMIN|DOCTOR") String role
) {
}
