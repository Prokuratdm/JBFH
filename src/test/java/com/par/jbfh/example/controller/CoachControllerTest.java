package com.par.jbfh.example.controller;

import com.par.jbfh.ControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(CoachController.class)
class CoachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "COACH")
    void getMyTeam_withCoachRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/coach/my-team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Hockey Team"))
                .andExpect(jsonPath("$.message").value("You have COACH access"));
    }

    @Test
    @WithMockUser(roles = "MAIN_COACH")
    void getMyTeam_withMainCoachRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/coach/my-team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Hockey Team"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyTeam_withAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/coach/my-team"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COACH")
    void getSchedule_withCoachRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/coach/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule").isArray())
                .andExpect(jsonPath("$.schedule.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "MAIN_COACH")
    void getSchedule_withMainCoachRole_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/coach/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule").isArray());
    }

    @Test
    void getSchedule_withoutAuth_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/coach/schedule"))
                .andExpect(status().isForbidden());
    }
}