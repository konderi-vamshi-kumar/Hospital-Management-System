package com.hospitalmanagement.membership.dto;

public record StaffInvitationCreatedResponse(
        long id,
        long hospitalId,
        String role,
        String status,
        String createdAt,
        String expiresAt,
        String inviteCode
) {
}
