package com.hospitalmanagement.model;

public record User(
        long id,
        String username,
        String passwordHash,
        String createdAt
) {
}
