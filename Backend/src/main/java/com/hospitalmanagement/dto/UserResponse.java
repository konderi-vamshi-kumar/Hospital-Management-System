package com.hospitalmanagement.dto;

public record UserResponse(
        long id,
        String username,
        String createdAt
) {
}
