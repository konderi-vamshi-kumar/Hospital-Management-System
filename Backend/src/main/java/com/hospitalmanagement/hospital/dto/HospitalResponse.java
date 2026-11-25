package com.hospitalmanagement.hospital.dto;

public record HospitalResponse(
        long id,
        String name,
        String status,
        String createdAt
) {
}
