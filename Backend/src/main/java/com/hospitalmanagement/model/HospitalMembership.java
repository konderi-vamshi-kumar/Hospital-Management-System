package com.hospitalmanagement.model;

public record HospitalMembership(
        long hospitalId,
        String role,
        String status
) {
}
