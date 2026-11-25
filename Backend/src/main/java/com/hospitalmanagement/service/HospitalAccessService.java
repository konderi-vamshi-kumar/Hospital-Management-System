package com.hospitalmanagement.service;

import com.hospitalmanagement.repository.UserRepository;
import com.hospitalmanagement.hospital.repository.HospitalRepository;
import org.springframework.stereotype.Service;

@Service
public class HospitalAccessService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;

    public HospitalAccessService(
            UserRepository userRepository,
            HospitalRepository hospitalRepository
    ) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public boolean canAccessHospital(String username, long hospitalId) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "The authenticated account could not be found."
                ));
        if (userRepository.findGlobalRolesByUserId(user.id()).contains("SUPER_ADMIN")) {
            return true;
        }
        return hospitalRepository.findById(hospitalId)
                .filter(hospital -> hospital.status().equals("ACTIVE"))
                .isPresent()
                && userRepository.hasActiveHospitalMembership(user.id(), hospitalId);
    }
}
