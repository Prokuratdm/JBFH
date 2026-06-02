package com.par.jbfh.auth.controller;

import com.par.jbfh.ControllerTest;
import com.par.jbfh.auth.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @Test
    void getAllRoles_shouldReturnRoles() throws Exception {
        when(roleService.getAllRoleNames()).thenReturn(
                List.of("ROLE_ADMIN", "ROLE_CLUB", "ROLE_METHODIST")
        );

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1]").value("ROLE_CLUB"))
                .andExpect(jsonPath("$[2]").value("ROLE_METHODIST"));
    }
}