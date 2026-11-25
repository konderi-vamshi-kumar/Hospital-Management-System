package com.studentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @NotBlank(message = "Username is required.")
        @Size(max = 30, message = "Username cannot be longer than 30 characters.")
        String username,

        @NotBlank(message = "Password is required.")
        @Size(max = 72, message = "Password cannot be longer than 72 characters.")
        String password
) {
}
