package com.hospitalmanagement.service;

import com.hospitalmanagement.dto.HospitalMembershipResponse;
import com.hospitalmanagement.dto.UserResponse;
import com.hospitalmanagement.exception.InvalidInputException;
import com.hospitalmanagement.exception.UsernameAlreadyExistsException;
import com.hospitalmanagement.model.User;
import com.hospitalmanagement.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse signUp(String username, String password) {
        String cleanUsername = username.trim();

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidInputException("Password must use 72 UTF-8 bytes or fewer.");
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new UsernameAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(password);

        try {
            userRepository.insert(cleanUsername, passwordHash);
            User user = userRepository.findByUsername(cleanUsername)
                    .orElseThrow(() -> new IllegalStateException(
                            "The new account could not be found."
                    ));
            userRepository.insertGlobalRole(user.id(), "PATIENT");
            return toUserResponse(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("The signed-in account could not be found."));
        return toUserResponse(user);
    }

    @Transactional
    public void ensureSuperAdmin(String username, String password) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.insert(username, passwordEncoder.encode(password));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "The configured super-admin account could not be found."
                ));
        userRepository.removeGlobalRole(user.id(), "PATIENT");
        userRepository.insertGlobalRole(user.id(), "SUPER_ADMIN");
    }

    private UserResponse toUserResponse(User user) {
        List<HospitalMembershipResponse> memberships = userRepository
                .findHospitalMembershipsByUserId(user.id())
                .stream()
                .map(membership -> new HospitalMembershipResponse(
                        membership.hospitalId(),
                        membership.role(),
                        membership.status()
                ))
                .toList();
        return new UserResponse(
                user.id(),
                user.username(),
                user.createdAt(),
                userRepository.findGlobalRolesByUserId(user.id()),
                memberships
        );
    }
}
