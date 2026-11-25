package com.hospitalmanagement.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HospitalCreateRequest(
        @NotBlank @Size(max = 120) String name
) {
}
