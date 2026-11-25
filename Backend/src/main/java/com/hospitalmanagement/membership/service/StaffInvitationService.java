package com.hospitalmanagement.membership.service;

import com.hospitalmanagement.exception.InvalidInputException;
import com.hospitalmanagement.exception.ResourceConflictException;
import com.hospitalmanagement.exception.ResourceNotFoundException;
import com.hospitalmanagement.hospital.repository.HospitalRepository;
import com.hospitalmanagement.membership.dto.AcceptedStaffInvitationResponse;
import com.hospitalmanagement.membership.dto.StaffInvitationCreateRequest;
import com.hospitalmanagement.membership.dto.StaffInvitationCreatedResponse;
import com.hospitalmanagement.membership.dto.StaffInvitationResponse;
import com.hospitalmanagement.membership.model.StaffInvitation;
import com.hospitalmanagement.membership.repository.HospitalMembershipRepository;
import com.hospitalmanagement.membership.repository.StaffInvitationRepository;
import com.hospitalmanagement.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffInvitationService {

    private static final Set<String> INVITABLE_ROLES = Set.of("HOSPITAL_ADMIN", "DOCTOR");

    private final StaffInvitationRepository invitationRepository;
    private final HospitalMembershipRepository membershipRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public StaffInvitationService(
            StaffInvitationRepository invitationRepository,
            HospitalMembershipRepository membershipRepository,
            HospitalRepository hospitalRepository,
            UserRepository userRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.membershipRepository = membershipRepository;
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StaffInvitationCreatedResponse createInvitation(
            long hospitalId,
            String inviterUsername,
            StaffInvitationCreateRequest request
    ) {
        if (!INVITABLE_ROLES.contains(request.role())) {
            throw new InvalidInputException("Choose a supported hospital staff role.");
        }

        var inviter = userRepository.findByUsername(inviterUsername)
                .orElseThrow(() -> new IllegalStateException(
                        "The authenticated account could not be found."
                ));
        String inviteCode = generateInviteCode();
        StaffInvitation invitation = invitationRepository.create(
                hospitalId,
                request.role(),
                hashInviteCode(inviteCode),
                inviter.id()
        );
        return new StaffInvitationCreatedResponse(
                invitation.id(),
                invitation.hospitalId(),
                invitation.role(),
                invitation.status(),
                invitation.createdAt(),
                invitation.expiresAt(),
                inviteCode
        );
    }

    @Transactional(readOnly = true)
    public List<StaffInvitationResponse> listInvitations(long hospitalId) {
        return invitationRepository.findByHospitalId(hospitalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revokeInvitation(long hospitalId, long invitationId) {
        if (invitationRepository.revoke(invitationId, hospitalId)) {
            return;
        }
        StaffInvitation invitation = invitationRepository
                .findByIdAndHospitalId(invitationId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff invitation not found in this hospital."
                ));
        throw new ResourceConflictException(
                invitation.status().equals("EXPIRED")
                        ? "This staff invitation has expired."
                        : "This staff invitation has already been used or revoked."
        );
    }

    @Transactional
    public AcceptedStaffInvitationResponse acceptInvitation(
            String username,
            String inviteCode
    ) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "The authenticated account could not be found."
                ));
        StaffInvitation invitation = invitationRepository
                .findByTokenHash(hashInviteCode(inviteCode))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "That invitation code is invalid."
                ));

        if (!invitation.status().equals("PENDING")) {
            throw new ResourceConflictException(
                    invitation.status().equals("EXPIRED")
                            ? "This staff invitation has expired."
                            : "This staff invitation has already been used or revoked."
            );
        }
        hospitalRepository.findById(invitation.hospitalId())
                .filter(hospital -> hospital.status().equals("ACTIVE"))
                .orElseThrow(() -> new ResourceConflictException(
                        "This invitation belongs to an inactive hospital."
                ));
        if (!invitationRepository.markAccepted(invitation.id(), user.id())) {
            throw new ResourceConflictException(
                    "This staff invitation has expired or has already been used."
            );
        }
        userRepository.activateHospitalMembership(
                user.id(),
                invitation.hospitalId(),
                invitation.role()
        );
        return new AcceptedStaffInvitationResponse(
                invitation.hospitalId(),
                invitation.role(),
                "ACTIVE"
        );
    }

    private String generateInviteCode() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashInviteCode(String inviteCode) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(inviteCode.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private StaffInvitationResponse toResponse(StaffInvitation invitation) {
        return new StaffInvitationResponse(
                invitation.id(),
                invitation.hospitalId(),
                invitation.role(),
                invitation.status(),
                invitation.createdAt(),
                invitation.expiresAt()
        );
    }
}
