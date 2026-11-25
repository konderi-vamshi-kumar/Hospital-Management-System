package com.hospitalmanagement.membership.dto;

public record AcceptedStaffInvitationResponse(
        long hospitalId,
        String role,
        String status
) {
}
