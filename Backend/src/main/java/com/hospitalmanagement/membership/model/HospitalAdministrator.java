package com.hospitalmanagement.membership.model;

public record HospitalAdministrator(
        long membershipId,
        long userId,
        long hospitalId,
        String username,
        String status,
        String createdAt
) {
}
