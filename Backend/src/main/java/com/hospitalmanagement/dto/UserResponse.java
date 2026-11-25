package com.hospitalmanagement.dto;

import java.util.List;

public record UserResponse(
        long id,
        String username,
        String createdAt,
        List<String> roles,
        List<HospitalMembershipResponse> hospitalMemberships
) {
}
