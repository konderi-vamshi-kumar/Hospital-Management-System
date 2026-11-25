package com.hospitalmanagement.hospital.controller;

import com.hospitalmanagement.branch.dto.BranchCreateRequest;
import com.hospitalmanagement.branch.dto.BranchResponse;
import com.hospitalmanagement.branch.dto.BranchUpdateRequest;
import com.hospitalmanagement.department.dto.DepartmentCreateRequest;
import com.hospitalmanagement.department.dto.DepartmentResponse;
import com.hospitalmanagement.department.dto.DepartmentUpdateRequest;
import com.hospitalmanagement.hospital.dto.HospitalResponse;
import com.hospitalmanagement.hospital.service.PlatformHospitalService;
import com.hospitalmanagement.membership.dto.StaffInvitationCreateRequest;
import com.hospitalmanagement.membership.dto.StaffInvitationCreatedResponse;
import com.hospitalmanagement.membership.dto.StaffInvitationResponse;
import com.hospitalmanagement.membership.service.HospitalAdminService;
import com.hospitalmanagement.membership.service.StaffInvitationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hospital-admin")
public class HospitalAdminController {

    private final HospitalAdminService hospitalAdminService;
    private final PlatformHospitalService platformHospitalService;
    private final StaffInvitationService staffInvitationService;

    public HospitalAdminController(
            HospitalAdminService hospitalAdminService,
            PlatformHospitalService platformHospitalService,
            StaffInvitationService staffInvitationService
    ) {
        this.hospitalAdminService = hospitalAdminService;
        this.platformHospitalService = platformHospitalService;
        this.staffInvitationService = staffInvitationService;
    }

    @GetMapping("/hospitals")
    @PreAuthorize("@hospitalAccessService.hasActiveHospitalAdminMembership(authentication.name)")
    public List<HospitalResponse> listHospitals(Authentication authentication) {
        return hospitalAdminService.listHospitals(authentication.getName());
    }

    @GetMapping("/hospitals/{hospitalId}/branches")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public List<BranchResponse> listBranches(@PathVariable long hospitalId) {
        return platformHospitalService.listBranches(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public BranchResponse createBranch(
            @PathVariable long hospitalId,
            @Valid @RequestBody BranchCreateRequest request
    ) {
        return platformHospitalService.createBranch(hospitalId, request);
    }

    @PutMapping("/hospitals/{hospitalId}/branches/{branchId}")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public BranchResponse updateBranch(
            @PathVariable long hospitalId,
            @PathVariable long branchId,
            @Valid @RequestBody BranchUpdateRequest request
    ) {
        return platformHospitalService.updateBranch(hospitalId, branchId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/departments")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public List<DepartmentResponse> listDepartments(@PathVariable long hospitalId) {
        return platformHospitalService.listDepartments(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/departments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public DepartmentResponse createDepartment(
            @PathVariable long hospitalId,
            @Valid @RequestBody DepartmentCreateRequest request
    ) {
        return platformHospitalService.createDepartment(hospitalId, request);
    }

    @PutMapping("/hospitals/{hospitalId}/departments/{departmentId}")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public DepartmentResponse updateDepartment(
            @PathVariable long hospitalId,
            @PathVariable long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return platformHospitalService.updateDepartment(hospitalId, departmentId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/invitations")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public List<StaffInvitationResponse> listInvitations(@PathVariable long hospitalId) {
        return staffInvitationService.listInvitations(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public StaffInvitationCreatedResponse createInvitation(
            @PathVariable long hospitalId,
            @Valid @RequestBody StaffInvitationCreateRequest request,
            Authentication authentication
    ) {
        return staffInvitationService.createInvitation(
                hospitalId,
                authentication.getName(),
                request
        );
    }

    @DeleteMapping("/hospitals/{hospitalId}/invitations/{invitationId}")
    @PreAuthorize("@hospitalAccessService.isHospitalAdmin(authentication.name, #hospitalId)")
    public ResponseEntity<Void> revokeInvitation(
            @PathVariable long hospitalId,
            @PathVariable long invitationId
    ) {
        staffInvitationService.revokeInvitation(hospitalId, invitationId);
        return ResponseEntity.noContent().build();
    }
}
