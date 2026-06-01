package com.par.jbfh.auth.service;

import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    @Test
    void initRoles_shouldCreateMissingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        roleService.initRoles();

        verify(roleRepository, times(6)).save(any(Role.class));
    }

    @Test
    void initRoles_shouldNotCreateExistingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role("ROLE_ADMIN")));

        roleService.initRoles();

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getAllRoleNames_shouldReturnAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(
                new Role("ROLE_ADMIN"),
                new Role("ROLE_CLUB"),
                new Role("ROLE_COACH")
        ));

        List<String> roles = roleService.getAllRoleNames();

        assertEquals(3, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_CLUB"));
        assertTrue(roles.contains("ROLE_COACH"));
    }

    @Test
    void findByName_shouldReturnRole() {
        Role role = new Role("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));

        Role found = roleService.findByName("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", found.getName());
    }

    @Test
    void findByName_shouldThrowWhenNotFound() {
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> roleService.findByName("ROLE_UNKNOWN"));
    }

    @Test
    void getFixedRoles_shouldReturnAllSixRoles() {
        Set<String> fixedRoles = roleService.getFixedRoles();

        assertEquals(6, fixedRoles.size());
        assertTrue(fixedRoles.contains("ROLE_ADMIN"));
        assertTrue(fixedRoles.contains("ROLE_CLUB"));
        assertTrue(fixedRoles.contains("ROLE_METHODIST"));
        assertTrue(fixedRoles.contains("ROLE_CLUB_METHODIST"));
        assertTrue(fixedRoles.contains("ROLE_COACH"));
        assertTrue(fixedRoles.contains("ROLE_MAIN_COACH"));
    }
}