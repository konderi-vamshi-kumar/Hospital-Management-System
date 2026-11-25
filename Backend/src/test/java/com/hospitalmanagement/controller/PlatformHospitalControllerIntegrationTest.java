package com.hospitalmanagement.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitalmanagement.service.AuthService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:file:platform-hospital-test?mode=memory&cache=shared",
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.hikari.minimum-idle=1"
})
class PlatformHospitalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Test
    void platformRoutesRequireSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/platform/hospitals"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/platform/hospitals")
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You are not allowed to perform this action."));
    }

    @Test
    void superAdminCanOnboardHospitalResourcesAndAssignAnExistingAdministrator()
            throws Exception {
        String adminUsername = "admin_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        authService.signUp(adminUsername, "learning123");
        var superAdmin = user("platform_admin").roles("SUPER_ADMIN");
        String hospitalName = "Hospital " + UUID.randomUUID();

        MvcResult hospitalResult = mockMvc.perform(post("/api/platform/hospitals")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", hospitalName))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(hospitalName))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        long hospitalId = objectMapper.readTree(
                        hospitalResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        MvcResult branchResult = mockMvc.perform(post(
                                "/api/platform/hospitals/" + hospitalId + "/branches")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Central",
                                "address", "12 Main Road",
                                "phone", "0112345678"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalId").value(hospitalId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        long branchId = objectMapper.readTree(
                        branchResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(post("/api/platform/hospitals/" + hospitalId + "/branches")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "central",
                                "address", "Duplicate address"
                        ))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/platform/hospitals/" + hospitalId + "/departments")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Cardiology",
                                "description", "Heart care"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cardiology"));

        mockMvc.perform(post("/api/platform/hospitals/" + hospitalId + "/administrators")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", adminUsername))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(adminUsername))
                .andExpect(jsonPath("$.role").value("HOSPITAL_ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/platform/hospitals/" + hospitalId + "/administrators")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", adminUsername))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/platform/hospitals/" + hospitalId + "/administrators")
                        .with(superAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/platform/hospitals/" + hospitalId + "/branches/" + branchId)
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Central",
                                "address", "12 Main Road",
                                "phone", "0112345678",
                                "status", "INACTIVE"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(put("/api/platform/hospitals/" + hospitalId)
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", hospitalName,
                                "status", "INACTIVE"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/api/platform/hospitals/" + hospitalId)
                        .with(superAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(hospitalName));
    }

    @Test
    void rejectsUnknownHospitalAndUnknownAdministrator() throws Exception {
        var superAdmin = user("platform_admin").roles("SUPER_ADMIN");

        mockMvc.perform(get("/api/platform/hospitals/999999")
                        .with(superAdmin))
                .andExpect(status().isNotFound());

        MvcResult hospitalResult = mockMvc.perform(post("/api/platform/hospitals")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Lookup " + UUID.randomUUID()))))
                .andExpect(status().isCreated())
                .andReturn();
        long hospitalId = objectMapper.readTree(
                        hospitalResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(post("/api/platform/hospitals/" + hospitalId + "/administrators")
                        .with(superAdmin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username",
                                "missing_" + UUID.randomUUID().toString().replace("-", "")
                                        .substring(0, 20)
                        ))))
                .andExpect(status().isNotFound());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
