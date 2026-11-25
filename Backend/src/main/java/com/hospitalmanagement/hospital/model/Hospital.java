package com.hospitalmanagement.hospital.model;

public record Hospital(
        long id,
        String name,
        String status,
        String createdAt
) {
}
