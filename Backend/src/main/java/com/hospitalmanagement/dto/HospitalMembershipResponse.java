package com.hospitalmanagement.dto;

public record HospitalMembershipResponse(
        long hospitalId,
        String role,
        String status
) {
}
