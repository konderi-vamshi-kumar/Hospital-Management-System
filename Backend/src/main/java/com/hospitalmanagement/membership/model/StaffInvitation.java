package com.hospitalmanagement.membership.model;

public record StaffInvitation(
        long id,
        long hospitalId,
        String role,
        String status,
        String createdAt,
        String expiresAt
) {
}
