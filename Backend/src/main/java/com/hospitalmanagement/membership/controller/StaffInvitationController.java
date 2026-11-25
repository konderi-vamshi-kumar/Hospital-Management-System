package com.hospitalmanagement.membership.controller;

import com.hospitalmanagement.membership.dto.AcceptStaffInvitationRequest;
import com.hospitalmanagement.membership.dto.AcceptedStaffInvitationResponse;
import com.hospitalmanagement.membership.service.StaffInvitationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
public class StaffInvitationController {

    private final StaffInvitationService staffInvitationService;

    public StaffInvitationController(StaffInvitationService staffInvitationService) {
        this.staffInvitationService = staffInvitationService;
    }

    @PostMapping("/accept")
    public AcceptedStaffInvitationResponse acceptInvitation(
            @Valid @RequestBody AcceptStaffInvitationRequest request,
            Authentication authentication
    ) {
        return staffInvitationService.acceptInvitation(
                authentication.getName(),
                request.inviteCode()
        );
    }
}
