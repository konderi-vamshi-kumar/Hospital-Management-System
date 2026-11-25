package com.hospitalmanagement.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 30) String phone
) {
}
