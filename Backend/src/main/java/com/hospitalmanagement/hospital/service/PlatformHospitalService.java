package com.hospitalmanagement.hospital.service;

import com.hospitalmanagement.branch.dto.BranchCreateRequest;
import com.hospitalmanagement.branch.dto.BranchResponse;
import com.hospitalmanagement.branch.dto.BranchUpdateRequest;
import com.hospitalmanagement.branch.model.Branch;
import com.hospitalmanagement.branch.repository.BranchRepository;
import com.hospitalmanagement.department.dto.DepartmentCreateRequest;
import com.hospitalmanagement.department.dto.DepartmentResponse;
import com.hospitalmanagement.department.dto.DepartmentUpdateRequest;
import com.hospitalmanagement.department.model.Department;
import com.hospitalmanagement.department.repository.DepartmentRepository;
import com.hospitalmanagement.dto.UserResponse;
import com.hospitalmanagement.exception.ResourceConflictException;
import com.hospitalmanagement.exception.ResourceNotFoundException;
import com.hospitalmanagement.hospital.dto.HospitalCreateRequest;
import com.hospitalmanagement.hospital.dto.HospitalResponse;
import com.hospitalmanagement.hospital.dto.HospitalUpdateRequest;
import com.hospitalmanagement.hospital.model.Hospital;
import com.hospitalmanagement.hospital.repository.HospitalRepository;
import com.hospitalmanagement.membership.dto.AdministratorAssignmentRequest;
import com.hospitalmanagement.membership.dto.HospitalAdministratorResponse;
import com.hospitalmanagement.membership.model.HospitalAdministrator;
import com.hospitalmanagement.membership.repository.HospitalMembershipRepository;
import com.hospitalmanagement.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformHospitalService {

    private final HospitalRepository hospitalRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public PlatformHospitalService(
            HospitalRepository hospitalRepository,
            BranchRepository branchRepository,
            DepartmentRepository departmentRepository,
            HospitalMembershipRepository membershipRepository,
            UserRepository userRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.branchRepository = branchRepository;
        this.departmentRepository = departmentRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> listHospitals() {
        return hospitalRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public HospitalResponse createHospital(HospitalCreateRequest request) {
        return toResponse(hospitalRepository.insert(request.name().trim()));
    }

    @Transactional(readOnly = true)
    public HospitalResponse getHospital(long hospitalId) {
        return toResponse(requireHospital(hospitalId));
    }

    @Transactional
    public HospitalResponse updateHospital(long hospitalId, HospitalUpdateRequest request) {
        Hospital hospital = hospitalRepository.update(
                        hospitalId,
                        request.name().trim(),
                        request.status()
                )
                .orElseThrow(() -> hospitalNotFound(hospitalId));
        return toResponse(hospital);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> listBranches(long hospitalId) {
        requireHospital(hospitalId);
        return branchRepository.findByHospitalId(hospitalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BranchResponse createBranch(long hospitalId, BranchCreateRequest request) {
        requireHospital(hospitalId);
        Branch branch = branchRepository.insert(
                        hospitalId,
                        request.name().trim(),
                        request.address().trim(),
                        cleanOptional(request.phone())
                )
                .orElseThrow(() -> new ResourceConflictException(
                        "A branch with that name already exists in this hospital."
                ));
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(
            long hospitalId,
            long branchId,
            BranchUpdateRequest request
    ) {
        requireHospital(hospitalId);
        var updated = branchRepository.update(
                branchId,
                hospitalId,
                request.name().trim(),
                request.address().trim(),
                cleanOptional(request.phone()),
                request.status()
        );
        if (updated.isPresent()) {
            return toResponse(updated.get());
        }
        if (branchRepository.findByIdAndHospitalId(branchId, hospitalId).isEmpty()) {
            throw new ResourceNotFoundException("Branch not found in this hospital.");
        }
        throw new ResourceConflictException(
                "A branch with that name already exists in this hospital."
        );
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments(long hospitalId) {
        requireHospital(hospitalId);
        return departmentRepository.findByHospitalId(hospitalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse createDepartment(
            long hospitalId,
            DepartmentCreateRequest request
    ) {
        requireHospital(hospitalId);
        Department department = departmentRepository.insert(
                        hospitalId,
                        request.name().trim(),
                        cleanOptional(request.description())
                )
                .orElseThrow(() -> new ResourceConflictException(
                        "A department with that name already exists in this hospital."
                ));
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(
            long hospitalId,
            long departmentId,
            DepartmentUpdateRequest request
    ) {
        requireHospital(hospitalId);
        var updated = departmentRepository.update(
                departmentId,
                hospitalId,
                request.name().trim(),
                cleanOptional(request.description()),
                request.status()
        );
        if (updated.isPresent()) {
            return toResponse(updated.get());
        }
        if (departmentRepository.findByIdAndHospitalId(departmentId, hospitalId).isEmpty()) {
            throw new ResourceNotFoundException("Department not found in this hospital.");
        }
        throw new ResourceConflictException(
                "A department with that name already exists in this hospital."
        );
    }

    @Transactional(readOnly = true)
    public List<HospitalAdministratorResponse> listAdministrators(long hospitalId) {
        requireHospital(hospitalId);
        return membershipRepository.findAdministratorsByHospitalId(hospitalId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HospitalAdministratorResponse assignAdministrator(
            long hospitalId,
            AdministratorAssignmentRequest request
    ) {
        requireHospital(hospitalId);
        String username = request.username().trim();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No registered account was found with that username."
                ));
        HospitalAdministrator administrator = membershipRepository.assignAdministrator(
                user.id(),
                hospitalId,
                user.username()
        );
        return toResponse(administrator);
    }

    private Hospital requireHospital(long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> hospitalNotFound(hospitalId));
    }

    private ResourceNotFoundException hospitalNotFound(long hospitalId) {
        return new ResourceNotFoundException("Hospital " + hospitalId + " was not found.");
    }

    private String cleanOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private HospitalResponse toResponse(Hospital hospital) {
        return new HospitalResponse(
                hospital.id(),
                hospital.name(),
                hospital.status(),
                hospital.createdAt()
        );
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.id(),
                branch.hospitalId(),
                branch.name(),
                branch.address(),
                branch.phone(),
                branch.status(),
                branch.createdAt()
        );
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.id(),
                department.hospitalId(),
                department.name(),
                department.description(),
                department.status(),
                department.createdAt()
        );
    }

    private HospitalAdministratorResponse toResponse(HospitalAdministrator administrator) {
        return new HospitalAdministratorResponse(
                administrator.membershipId(),
                administrator.userId(),
                administrator.hospitalId(),
                administrator.username(),
                "HOSPITAL_ADMIN",
                administrator.status(),
                administrator.createdAt()
        );
    }
}
