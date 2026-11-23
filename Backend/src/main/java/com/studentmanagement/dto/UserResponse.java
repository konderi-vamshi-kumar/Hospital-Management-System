package com.studentmanagement.dto;

public record UserResponse(
        long id,
        String username,
        String createdAt
) {
}
