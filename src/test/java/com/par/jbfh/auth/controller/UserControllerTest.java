package com.par.jbfh.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.jbfh.ControllerTest;
import com.par.jbfh.auth.dto.ChangePasswordRequest;
import com.par.jbfh.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // --- POST /api/v1/users ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_withAdminRole_shouldReturnCreated() throws Exception {
        String requestJson = """
                {
                    "username": "newuser",
                    "password": "pass123",
                    "roles": ["ROLE_COACH"],
                    "clubId": "%s"
                }
                """.formatted(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void createUser_withClubRole_shouldReturnCreated() throws Exception {
        String requestJson = """
                {
                    "username": "newuser",
                    "password": "pass123",
                    "roles": ["ROLE_COACH"]
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "COACH")
    void createUser_withCoachRole_shouldReturnForbidden() throws Exception {
        String requestJson = """
                {
                    "username": "newuser",
                    "password": "pass123",
                    "roles": ["ROLE_COACH"]
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_withoutAuth_shouldReturnForbidden() throws Exception {
        String requestJson = """
                {
                    "username": "newuser",
                    "password": "pass123",
                    "roles": ["ROLE_COACH"]
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/users/{id} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void getUserById_withClubRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // --- PUT /api/v1/users/me/password ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeOwnPassword_withAdminRole_shouldReturnOk() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void changeOwnPassword_withClubRole_shouldReturnOk() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changeOwnPassword_withoutAuth_shouldReturnForbidden() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass");

        mockMvc.perform(put("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- PUT /api/v1/users/{id}/password ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserPasswordAsAdmin_withAdminRole_shouldReturnOk() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newAdminPass");

        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void changeUserPasswordAsAdmin_withClubRole_shouldReturnForbidden() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass");

        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserPasswordAsAdmin_withoutAuth_shouldReturnForbidden() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass");

        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID() + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}