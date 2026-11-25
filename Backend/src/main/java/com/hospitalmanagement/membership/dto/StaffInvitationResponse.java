package com.hospitalmanagement.membership.dto;

public record StaffInvitationResponse(
        long id,
        long hospitalId,
        String role,
        String status,
        String createdAt,
        String expiresAt
) {
}
