package com.hospitalmanagement.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AcceptStaffInvitationRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String inviteCode
) {
}
