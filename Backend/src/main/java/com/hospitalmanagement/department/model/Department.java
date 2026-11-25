package com.hospitalmanagement.department.model;

public record Department(
        long id,
        long hospitalId,
        String name,
        String description,
        String status,
        String createdAt
) {
}
