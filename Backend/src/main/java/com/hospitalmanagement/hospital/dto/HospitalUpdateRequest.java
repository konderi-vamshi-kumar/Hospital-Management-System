package com.hospitalmanagement.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HospitalUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status
) {
}
