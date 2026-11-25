package com.hospitalmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "Username is required.")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username can use letters, numbers, and underscores only.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        String password
) {
}
