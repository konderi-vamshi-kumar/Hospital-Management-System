package com.hospitalmanagement.config;

import com.hospitalmanagement.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    private final AuthService authService;
    private final String username;
    private final String password;

    public SuperAdminBootstrap(
            AuthService authService,
            @Value("${app.bootstrap-super-admin.username:}") String username,
            @Value("${app.bootstrap-super-admin.password:}") String password
    ) {
        this.authService = authService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalStateException(
                    "SUPER_ADMIN_USERNAME must be 3-30 letters, numbers, or underscores."
            );
        }
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "SUPER_ADMIN_PASSWORD must be at least 12 characters and at most 72 UTF-8 bytes."
            );
        }

        authService.ensureSuperAdmin(username, password);
    }
}
