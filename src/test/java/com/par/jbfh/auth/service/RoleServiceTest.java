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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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
    void initRolesShouldCreateMissingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());

        roleService.initRoles();

        verify(roleRepository, times(6)).save(any(Role.class));
    }

    @Test
    void initRolesShouldSkipExistingRoles() {
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role("ROLE_ADMIN")));

        roleService.initRoles();

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getAllRoleNamesShouldReturnRoleNames() {
        when(roleRepository.findAll()).thenReturn(List.of(
                new Role("ROLE_ADMIN"),
                new Role("ROLE_COACH")
        ));

        List<String> roles = roleService.getAllRoleNames();

        assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_COACH");
    }

    @Test
    void findByNameShouldReturnRoleWhenExists() {
        Role role = new Role("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));

        Role found = roleService.findByName("ROLE_ADMIN");

        assertThat(found).isEqualTo(role);
    }

    @Test
    void findByNameShouldThrowWhenNotFound() {
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findByName("NONEXISTENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found: NONEXISTENT");
    }

    @Test
    void getFixedRolesShouldReturnAllSixRoles() {
        Set<String> fixedRoles = roleService.getFixedRoles();

        assertThat(fixedRoles).containsExactlyInAnyOrder(
                "ROLE_ADMIN",
                "ROLE_CLUB",
                "ROLE_METHODIST",
                "ROLE_CLUB_METHODIST",
                "ROLE_COACH",
                "ROLE_MAIN_COACH"
        );
    }
}