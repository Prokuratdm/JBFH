package com.par.jbfh.auth.service;

import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.RoleRepository;
import com.par.jbfh.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInitServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserInitService userInitService;

    @BeforeEach
    void setUp() {
        userInitService = new UserInitService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void shouldSkipWhenAdminAlreadyExists() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        userInitService.initDefaultAdmin();

        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByName(anyString());
    }

    @Test
    void shouldCreateDefaultAdminWhenNotExists() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        Role adminRole = new Role("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_password");

        userInitService.initDefaultAdmin();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("admin");
        assertThat(savedUser.getPassword()).isEqualTo("encoded_password");
        assertThat(savedUser.getEmail()).isEqualTo("admin@jbfh.by");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRoles()).containsExactly(adminRole);
    }

    @Test
    void shouldThrowWhenAdminRoleNotFound() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userInitService.initDefaultAdmin())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROLE_ADMIN not found");
    }
}