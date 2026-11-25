package com.hospitalmanagement.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BranchUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 30) String phone,
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status
) {
}
