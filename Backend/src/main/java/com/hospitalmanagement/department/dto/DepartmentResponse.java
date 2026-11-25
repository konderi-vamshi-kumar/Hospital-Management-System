package com.hospitalmanagement.department.dto;

public record DepartmentResponse(
        long id,
        long hospitalId,
        String name,
        String description,
        String status,
        String createdAt
) {
}
