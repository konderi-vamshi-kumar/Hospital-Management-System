package com.studentmanagement.service;

import com.studentmanagement.dto.UserResponse;
import com.studentmanagement.exception.InvalidCredentialsException;
import com.studentmanagement.exception.InvalidInputException;
import com.studentmanagement.exception.UsernameAlreadyExistsException;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException();
        }

        User user = userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new IllegalStateException("The new account could not be found."));

        return toUserResponse(user);
    }

    public UserResponse signIn(String username, String password) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.id(), user.username(), user.createdAt());
    }
}
