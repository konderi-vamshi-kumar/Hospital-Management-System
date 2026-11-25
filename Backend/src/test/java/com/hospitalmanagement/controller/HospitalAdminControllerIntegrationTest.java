package com.hospitalmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitalmanagement.hospital.model.Hospital;
import com.hospitalmanagement.hospital.repository.HospitalRepository;
import com.hospitalmanagement.membership.repository.HospitalMembershipRepository;
import com.hospitalmanagement.service.AuthService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:file:hospital-admin-test?mode=memory&cache=shared",
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.hikari.minimum-idle=1"
})
class HospitalAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private HospitalMembershipRepository membershipRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void hospitalAdminsCanManageOnlyTheirOwnBranchesAndDepartments() throws Exception {
        var fixture = createAdminFixture();
        Hospital otherHospital = hospitalRepository.insert("Other " + UUID.randomUUID());
        var admin = user(fixture.username()).roles("PATIENT");

        mockMvc.perform(get("/api/hospital-admin/hospitals").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(fixture.hospital().id()));

        MvcResult branchResult = mockMvc.perform(post(
                                "/api/hospital-admin/hospitals/"
                                        + fixture.hospital().id() + "/branches")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "East Wing",
                                "address", "42 Lake Road"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        long branchId = objectMapper.readTree(
                        branchResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(put("/api/hospital-admin/hospitals/"
                                + fixture.hospital().id() + "/branches/" + branchId)
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "East Wing",
                                "address", "43 Lake Road",
                                "status", "ACTIVE"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("43 Lake Road"));

        mockMvc.perform(post("/api/hospital-admin/hospitals/"
                                + fixture.hospital().id() + "/departments")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Neurology",
                                "description", "Brain and nerve care"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalId").value(fixture.hospital().id()));

        mockMvc.perform(get("/api/hospital-admin/hospitals/"
                                + otherHospital.id() + "/branches")
                        .with(admin))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/hospital-admin/hospitals/"
                                + otherHospital.id() + "/invitations")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "DOCTOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void invitationsAreOneTimeHashedAndActivateMembershipForSignedInUser()
            throws Exception {
        var fixture = createAdminFixture();
        var admin = user(fixture.username()).roles("PATIENT");
        String inviteeUsername = "invitee_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.signUp(inviteeUsername, "learning123");

        MvcResult invitationResult = mockMvc.perform(post(
                                "/api/hospital-admin/hospitals/"
                                        + fixture.hospital().id() + "/invitations")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "DOCTOR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("DOCTOR"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        var invitation = objectMapper.readTree(
                invitationResult.getResponse().getContentAsString());
        String inviteCode = invitation.get("inviteCode").asText();
        long invitationId = invitation.get("id").asLong();
        String storedHash = jdbcTemplate.queryForObject(
                "SELECT token_hash FROM staff_invitations WHERE id = ?",
                String.class,
                invitationId
        );
        assertNotEquals(inviteCode, storedHash);
        assertTrue(storedHash.matches("[0-9a-f]{64}"));

        mockMvc.perform(get("/api/hospital-admin/hospitals/"
                                + fixture.hospital().id() + "/invitations")
                        .with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inviteCode").doesNotExist());

        mockMvc.perform(post("/api/invitations/accept")
                        .with(user(inviteeUsername).roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("inviteCode", inviteCode))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalId").value(fixture.hospital().id()))
                .andExpect(jsonPath("$.role").value("DOCTOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/invitations/accept")
                        .with(user(inviteeUsername).roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("inviteCode", inviteCode))))
                .andExpect(status().isConflict());

        Integer membershipCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM hospital_memberships
                WHERE user_id = (SELECT id FROM users WHERE username = ?)
                  AND hospital_id = ?
                  AND role = 'DOCTOR'
                  AND status = 'ACTIVE'
                """,
                Integer.class,
                inviteeUsername,
                fixture.hospital().id()
        );
        assertTrue(membershipCount != null && membershipCount == 1);
    }

    @Test
    void nonAdminsCannotListHospitalAdminWorkspacesAndRevokedInvitesCannotBeAccepted()
            throws Exception {
        var fixture = createAdminFixture();
        var admin = user(fixture.username()).roles("PATIENT");
        String inviteeUsername = "pending_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.signUp(inviteeUsername, "learning123");

        mockMvc.perform(get("/api/hospital-admin/hospitals")
                        .with(user(inviteeUsername).roles("PATIENT")))
                .andExpect(status().isForbidden());

        MvcResult invitationResult = mockMvc.perform(post(
                                "/api/hospital-admin/hospitals/"
                                        + fixture.hospital().id() + "/invitations")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "HOSPITAL_ADMIN"))))
                .andExpect(status().isCreated())
                .andReturn();
        var invitation = objectMapper.readTree(
                invitationResult.getResponse().getContentAsString());
        String inviteCode = invitation.get("inviteCode").asText();
        long invitationId = invitation.get("id").asLong();

        mockMvc.perform(delete("/api/hospital-admin/hospitals/"
                                + fixture.hospital().id() + "/invitations/" + invitationId)
                        .with(admin)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/invitations/accept")
                        .with(user(inviteeUsername).roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("inviteCode", inviteCode))))
                .andExpect(status().isConflict());
    }

    @Test
    void expiredInvitationIsReportedAndCannotBeAccepted() throws Exception {
        var fixture = createAdminFixture();
        var admin = user(fixture.username()).roles("PATIENT");
        String inviteeUsername = "expired_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.signUp(inviteeUsername, "learning123");

        MvcResult invitationResult = mockMvc.perform(post(
                                "/api/hospital-admin/hospitals/"
                                        + fixture.hospital().id() + "/invitations")
                        .with(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("role", "DOCTOR"))))
                .andExpect(status().isCreated())
                .andReturn();
        var invitation = objectMapper.readTree(
                invitationResult.getResponse().getContentAsString());
        String inviteCode = invitation.get("inviteCode").asText();

        jdbcTemplate.update(
                """
                UPDATE staff_invitations
                SET expires_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now', '-1 day')
                WHERE id = ?
                """,
                invitation.get("id").asLong()
        );

        mockMvc.perform(get("/api/hospital-admin/hospitals/"
                                + fixture.hospital().id() + "/invitations")
                        .with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("EXPIRED"));

        mockMvc.perform(post("/api/invitations/accept")
                        .with(user(inviteeUsername).roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("inviteCode", inviteCode))))
                .andExpect(status().isConflict());
    }

    private AdminFixture createAdminFixture() {
        String username = "hospital_admin_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        var user = authService.signUp(username, "learning123");
        Hospital hospital = hospitalRepository.insert("Hospital " + UUID.randomUUID());
        membershipRepository.assignAdministrator(user.id(), hospital.id(), username);
        return new AdminFixture(username, hospital);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private record AdminFixture(String username, Hospital hospital) {
    }
}
