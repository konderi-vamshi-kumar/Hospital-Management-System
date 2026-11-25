package com.hospitalmanagement.hospital.controller;

import com.hospitalmanagement.branch.dto.BranchCreateRequest;
import com.hospitalmanagement.branch.dto.BranchResponse;
import com.hospitalmanagement.branch.dto.BranchUpdateRequest;
import com.hospitalmanagement.department.dto.DepartmentCreateRequest;
import com.hospitalmanagement.department.dto.DepartmentResponse;
import com.hospitalmanagement.department.dto.DepartmentUpdateRequest;
import com.hospitalmanagement.hospital.dto.HospitalCreateRequest;
import com.hospitalmanagement.hospital.dto.HospitalResponse;
import com.hospitalmanagement.hospital.dto.HospitalUpdateRequest;
import com.hospitalmanagement.hospital.service.PlatformHospitalService;
import com.hospitalmanagement.membership.dto.AdministratorAssignmentRequest;
import com.hospitalmanagement.membership.dto.HospitalAdministratorResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformHospitalController {

    private final PlatformHospitalService platformHospitalService;

    public PlatformHospitalController(PlatformHospitalService platformHospitalService) {
        this.platformHospitalService = platformHospitalService;
    }

    @GetMapping("/hospitals")
    public List<HospitalResponse> listHospitals() {
        return platformHospitalService.listHospitals();
    }

    @PostMapping("/hospitals")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse createHospital(@Valid @RequestBody HospitalCreateRequest request) {
        return platformHospitalService.createHospital(request);
    }

    @GetMapping("/hospitals/{hospitalId}")
    public HospitalResponse getHospital(@PathVariable long hospitalId) {
        return platformHospitalService.getHospital(hospitalId);
    }

    @PutMapping("/hospitals/{hospitalId}")
    public HospitalResponse updateHospital(
            @PathVariable long hospitalId,
            @Valid @RequestBody HospitalUpdateRequest request
    ) {
        return platformHospitalService.updateHospital(hospitalId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/branches")
    public List<BranchResponse> listBranches(@PathVariable long hospitalId) {
        return platformHospitalService.listBranches(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse createBranch(
            @PathVariable long hospitalId,
            @Valid @RequestBody BranchCreateRequest request
    ) {
        return platformHospitalService.createBranch(hospitalId, request);
    }

    @PutMapping("/hospitals/{hospitalId}/branches/{branchId}")
    public BranchResponse updateBranch(
            @PathVariable long hospitalId,
            @PathVariable long branchId,
            @Valid @RequestBody BranchUpdateRequest request
    ) {
        return platformHospitalService.updateBranch(hospitalId, branchId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/departments")
    public List<DepartmentResponse> listDepartments(@PathVariable long hospitalId) {
        return platformHospitalService.listDepartments(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(
            @PathVariable long hospitalId,
            @Valid @RequestBody DepartmentCreateRequest request
    ) {
        return platformHospitalService.createDepartment(hospitalId, request);
    }

    @PutMapping("/hospitals/{hospitalId}/departments/{departmentId}")
    public DepartmentResponse updateDepartment(
            @PathVariable long hospitalId,
            @PathVariable long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return platformHospitalService.updateDepartment(hospitalId, departmentId, request);
    }

    @GetMapping("/hospitals/{hospitalId}/administrators")
    public List<HospitalAdministratorResponse> listAdministrators(
            @PathVariable long hospitalId
    ) {
        return platformHospitalService.listAdministrators(hospitalId);
    }

    @PostMapping("/hospitals/{hospitalId}/administrators")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalAdministratorResponse assignAdministrator(
            @PathVariable long hospitalId,
            @Valid @RequestBody AdministratorAssignmentRequest request
    ) {
        return platformHospitalService.assignAdministrator(hospitalId, request);
    }
}
