package com.par.jbfh.auth.service;

import com.par.jbfh.auth.dto.ChangePasswordRequest;
import com.par.jbfh.auth.dto.CreateUserRequest;
import com.par.jbfh.auth.dto.UserResponse;
import com.par.jbfh.auth.entity.Club;
import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.ClubRepository;
import com.par.jbfh.auth.repository.RoleRepository;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ClubRepository clubRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    private Role roleAdmin;
    private Role roleClub;
    private Role roleCoach;
    private Club club;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, roleRepository, clubRepository, passwordEncoder);

        roleAdmin = new Role("ROLE_ADMIN");
        roleClub = new Role("ROLE_CLUB");
        roleCoach = new Role("ROLE_COACH");
        club = new Club("Test Club");
    }

    // --- Create User ---

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@test.com");
        request.setRoles(Set.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request, null);

        assertEquals("testuser", response.getUsername());
        assertEquals("test@test.com", response.getEmail());
        assertTrue(response.isEnabled());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        assertNull(response.getClubId());
    }

    @Test
    void createUser_shouldThrowWhenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existing");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowWhenRoleNotFound() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_UNKNOWN"));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request, null));
    }

    @Test
    void createUser_shouldThrowWhenClubRequiredButNotProvided() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_COACH"));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH")).thenReturn(Optional.of(roleCoach));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request, null));
    }

    @Test
    void createUser_shouldAssignClubWhenRequired() {
        UUID clubId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_COACH"));
        request.setClubId(clubId);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH")).thenReturn(Optional.of(roleCoach));
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request, null);

        assertEquals(clubId, response.getClubId());
        assertEquals("Test Club", response.getClubName());
    }

    @Test
    void createUser_shouldGetClubFromCreatorWhenNotProvided() {
        UUID clubId = UUID.randomUUID();
        club.setId(clubId);

        UserPrincipal principal = new UserPrincipal( "clubuser",UUID.randomUUID(), Set.of());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_COACH"));

        User creatorUser = new User();
        creatorUser.setClub(club);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH")).thenReturn(Optional.of(roleCoach));
        when(userRepository.findByUsername("clubuser")).thenReturn(Optional.of(creatorUser));
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request, principal);

        assertEquals(clubId, response.getClubId());
    }

    @Test
    void createUser_shouldNotRequireClubForAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("adminuser");
        request.setPassword("password123");
        request.setRoles(Set.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("adminuser")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(request, null);

        assertNull(response.getClubId());
    }

    // --- Get User ---

    @Test
    void getUserById_shouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setRoles(Set.of(roleAdmin));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(userId));
    }

    // --- Change Own Password ---

    @Test
    void changeOwnPassword_shouldChangeSuccessfully() {
        UserPrincipal principal = new UserPrincipal( "testuser", UUID.randomUUID(), Set.of());
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("oldPass123"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        userService.changeOwnPassword(principal, request);

        assertTrue(passwordEncoder.matches("newPass456", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void changeOwnPassword_shouldThrowWhenOldPasswordWrong() {
        UserPrincipal principal = new UserPrincipal( "testuser", UUID.randomUUID(), Set.of());
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctPass"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass456");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.changeOwnPassword(principal, request));
    }

    @Test
    void changeOwnPassword_shouldThrowWhenOldPasswordNull() {
        UserPrincipal principal = new UserPrincipal( "testuser", UUID.randomUUID(), Set.of());
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctPass"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass456");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.changeOwnPassword(principal, request));
    }

    @Test
    void changeOwnPassword_shouldThrowWhenPrincipalNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass456");

        assertThrows(IllegalArgumentException.class, () -> userService.changeOwnPassword(null, request));
    }

    // --- Change User Password As Admin ---

    @Test
    void changeUserPasswordAsAdmin_shouldChangeSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("targetuser");
        user.setPassword(passwordEncoder.encode("oldPass"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newAdminPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeUserPasswordAsAdmin(userId, request);

        assertTrue(passwordEncoder.matches("newAdminPass", user.getPassword()));
        verify(userRepository).save(user);
    }

    @Test
    void changeUserPasswordAsAdmin_shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.changeUserPasswordAsAdmin(userId, request));
    }
}