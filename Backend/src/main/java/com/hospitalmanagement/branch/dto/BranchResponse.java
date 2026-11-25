package com.hospitalmanagement.branch.dto;

public record BranchResponse(
        long id,
        long hospitalId,
        String name,
        String address,
        String phone,
        String status,
        String createdAt
) {
}
