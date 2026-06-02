package com.par.jbfh.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.jbfh.ControllerTest;
import com.par.jbfh.club.dto.CreateClubRequest;
import com.par.jbfh.club.dto.UpdateClubRequest;
import com.par.jbfh.club.service.ClubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(ClubController.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClubService clubService;

    // --- POST /api/v1/clubs ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClub_withAdminRole_shouldReturnCreated() throws Exception {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("New Club");
        request.setAddress("Address");
        request.setDescription("Description");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void createClub_withMethodistRole_shouldReturnForbidden() throws Exception {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("New Club");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void createClub_withClubRole_shouldReturnForbidden() throws Exception {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("New Club");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createClub_withoutAuth_shouldReturnForbidden() throws Exception {
        CreateClubRequest request = new CreateClubRequest();
        request.setName("New Club");

        mockMvc.perform(post("/api/v1/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/clubs ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClubs_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void getAllClubs_withMethodistRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void getAllClubs_withClubRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllClubs_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/clubs/{id} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClubById_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void getClubById_withMethodistRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void getClubById_withClubRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // --- PUT /api/v1/clubs/{id} ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateClub_withAdminRole_shouldReturnOk() throws Exception {
        UpdateClubRequest request = new UpdateClubRequest();
        request.setAddress("New Address");

        mockMvc.perform(put("/api/v1/clubs/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void updateClub_withMethodistRole_shouldReturnForbidden() throws Exception {
        UpdateClubRequest request = new UpdateClubRequest();
        request.setAddress("New Address");

        mockMvc.perform(put("/api/v1/clubs/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/v1/clubs/{id}/logo ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadLogo_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(multipart("/api/v1/clubs/" + UUID.randomUUID() + "/logo")
                        .file("file", "fake-image".getBytes()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void uploadLogo_withMethodistRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(multipart("/api/v1/clubs/" + UUID.randomUUID() + "/logo")
                        .file("file", "fake-image".getBytes()))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/clubs/{id}/logo ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLogo_withAdminRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID() + "/logo"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "METHODIST")
    void getLogo_withMethodistRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID() + "/logo"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLUB")
    void getLogo_withClubRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID() + "/logo"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLogo_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/clubs/" + UUID.randomUUID() + "/logo"))
                .andExpect(status().isForbidden());
    }
}