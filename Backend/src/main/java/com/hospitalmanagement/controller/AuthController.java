package com.hospitalmanagement.controller;

import com.hospitalmanagement.dto.SignInRequest;
import com.hospitalmanagement.dto.SignUpRequest;
import com.hospitalmanagement.dto.UserResponse;
import com.hospitalmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request.username(), request.password());
    }

    @PostMapping("/signin")
    public UserResponse signIn(@Valid @RequestBody SignInRequest request) {
        return authService.signIn(request.username(), request.password());
    }
}
