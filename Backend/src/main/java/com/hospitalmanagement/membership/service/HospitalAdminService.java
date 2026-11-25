package com.hospitalmanagement.membership.service;

import com.hospitalmanagement.exception.ResourceNotFoundException;
import com.hospitalmanagement.hospital.dto.HospitalResponse;
import com.hospitalmanagement.hospital.repository.HospitalRepository;
import com.hospitalmanagement.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HospitalAdminService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    public HospitalAdminService(
            HospitalRepository hospitalRepository,
            UserRepository userRepository
    ) {
        this.hospitalRepository = hospitalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> listHospitals(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The signed-in account could not be found."
                ));
        return hospitalRepository.findActiveForHospitalAdministrator(user.id()).stream()
                .map(hospital -> new HospitalResponse(
                        hospital.id(),
                        hospital.name(),
                        hospital.status(),
                        hospital.createdAt()
                ))
                .toList();
    }
}
