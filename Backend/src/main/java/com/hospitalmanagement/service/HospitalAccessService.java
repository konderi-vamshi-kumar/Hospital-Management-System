package com.hospitalmanagement.service;

import com.hospitalmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class HospitalAccessService {

    private final UserRepository userRepository;

    public HospitalAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean canAccessHospital(String username, long hospitalId) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "The authenticated account could not be found."
                ));
        return userRepository.findGlobalRolesByUserId(user.id()).contains("SUPER_ADMIN")
                || userRepository.hasActiveHospitalMembership(user.id(), hospitalId);
    }
}
