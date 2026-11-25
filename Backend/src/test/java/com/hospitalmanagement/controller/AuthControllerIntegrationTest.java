package com.hospitalmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitalmanagement.dto.UserResponse;
import com.hospitalmanagement.service.AuthService;
import com.hospitalmanagement.service.HospitalAccessService;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:file:hospital-management-test?mode=memory&cache=shared",
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.hikari.minimum-idle=1"
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private HospitalAccessService hospitalAccessService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void patientCanSignUpSignInAndUseAndEndASession() throws Exception {
        String username = "hospital_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String password = "learning123";
        CsrfState csrfState = getCsrfState();
        String signUpRequest = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        mockMvc.perform(csrfPost(
                        "/api/auth/signup",
                        signUpRequest,
                        csrfState
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.roles[0]").value("PATIENT"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(csrfPost("/api/auth/signup", signUpRequest, csrfState))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("That username is already taken."));

        mockMvc.perform(csrfPost(
                        "/api/auth/signin",
                        objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", "wrong-password"
                        )),
                        csrfState
                ))
                .andExpect(status().isUnauthorized());

        MvcResult signInResult = mockMvc.perform(csrfPost(
                        "/api/auth/signin",
                        objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        )),
                        csrfState
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.roles[0]").value("PATIENT"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) signInResult.getRequest().getSession(false);
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(post("/api/auth/signout")
                        .session(session)
                        .cookie(csrfState.cookie())
                        .header("X-XSRF-TOKEN", csrfState.token()))
                .andExpect(status().isNoContent());

        assertTrue(session.isInvalid());
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRejectUnauthenticatedRequestsAndInvalidCsrfTokens() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));

        String username = "hospital_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String request = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", "learning123"
        ));
        CsrfState csrfState = getCsrfState();

        mockMvc.perform(post("/api/auth/signup")
                        .cookie(csrfState.cookie())
                        .header("X-XSRF-TOKEN", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You are not allowed to perform this action."));
    }

    @Test
    void configuredSuperAdminRoleIsGrantedWithoutResettingAnExistingPassword() {
        String username = "admin_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.ensureSuperAdmin(username, "first-secure-password");

        authService.ensureSuperAdmin(username, "second-secure-password");

        UserResponse user = authService.currentUser(username);
        assertTrue(user.roles().contains("SUPER_ADMIN"));
        assertFalse(user.roles().contains("PATIENT"));
        assertTrue(authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        username,
                        "first-secure-password"
                )
        ).isAuthenticated());
        assertThrows(AuthenticationException.class, () -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        username,
                        "second-secure-password"
                )
        ));
    }

    @Test
    void hospitalMembershipsAreScopedAndSuperAdminsCanAccessAnyHospital() {
        String username = "member_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        UserResponse user = authService.signUp(username, "learning123");
        long hospitalId = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO hospitals (id, name) VALUES (?, ?)",
                hospitalId,
                "Test Hospital"
        );
        jdbcTemplate.update(
                """
                INSERT INTO hospital_memberships (user_id, hospital_id, role)
                VALUES (?, ?, 'HOSPITAL_ADMIN')
                """,
                user.id(),
                hospitalId
        );

        assertTrue(hospitalAccessService.canAccessHospital(username, hospitalId));
        assertFalse(hospitalAccessService.canAccessHospital(username, hospitalId + 1));

        String adminUsername = "root_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.ensureSuperAdmin(adminUsername, "platform-admin-password");
        assertTrue(hospitalAccessService.canAccessHospital(adminUsername, hospitalId + 1));
    }

    private CsrfState getCsrfState() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
        return new CsrfState(csrfCookie, token);
    }

    private MockHttpServletRequestBuilder csrfPost(
            String path,
            String body,
            CsrfState csrfState
    ) {
        return post(path)
                .cookie(csrfState.cookie())
                .header("X-XSRF-TOKEN", csrfState.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private record CsrfState(Cookie cookie, String token) {
    }
}
