package com.hospitalmanagement.membership.dto;

public record HospitalAdministratorResponse(
        long membershipId,
        long userId,
        long hospitalId,
        String username,
        String role,
        String status,
        String createdAt
) {
}
