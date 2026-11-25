package com.hospitalmanagement.branch.model;

public record Branch(
        long id,
        long hospitalId,
        String name,
        String address,
        String phone,
        String status,
        String createdAt
) {
}
