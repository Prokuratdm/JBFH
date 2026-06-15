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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, clubRepository, passwordEncoder);
    }

    // region createUser

    @Test
    void createUserShouldCreateAdminWithoutClub() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newadmin");
        request.setPassword("pass");
        request.setEmail("admin@test.com");
        request.setRoles(Set.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(new Role("ROLE_ADMIN")));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = userService.createUser(request, null);

        assertThat(response.getUsername()).isEqualTo("newadmin");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        assertThat(response.getRoles()).containsExactly("ROLE_ADMIN");
        assertThat(response.getClubId()).isNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserShouldCreateCoachWithClub() {
        UUID clubId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("coach1");
        request.setPassword("pass");
        request.setEmail("coach@test.com");
        request.setRoles(Set.of("ROLE_COACH"));
        request.setClubId(clubId);

        when(userRepository.existsByUsername("coach1")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH"))
                .thenReturn(Optional.of(new Role("ROLE_COACH")));
        Club testClub = new Club("Test Club");
        testClub.setId(clubId);
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(testClub));
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = userService.createUser(request, null);

        assertThat(response.getClubName()).isEqualTo("Test Club");
        assertThat(response.getClubId()).isEqualTo(clubId);
    }

    @Test
    void createUserShouldThrowWhenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existing");
        request.setPassword("pass");
        request.setEmail("test@test.com");
        request.setRoles(Set.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists: existing");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowWhenRoleNotFound() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("user");
        request.setPassword("pass");
        request.setEmail("test@test.com");
        request.setRoles(Set.of("NONEXISTENT_ROLE"));

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(roleRepository.findByName("NONEXISTENT_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found: NONEXISTENT_ROLE");
    }

    @Test
    void createUserShouldThrowWhenClubRequiredButNotProvided() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("coach");
        request.setPassword("pass");
        request.setEmail("coach@test.com");
        request.setRoles(Set.of("ROLE_COACH"));
        // no clubId

        when(userRepository.existsByUsername("coach")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH"))
                .thenReturn(Optional.of(new Role("ROLE_COACH")));

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club is required for this role");
    }

    @Test
    void createUserShouldThrowWhenClubNotFound() {
        UUID clubId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("coach");
        request.setPassword("pass");
        request.setEmail("coach@test.com");
        request.setRoles(Set.of("ROLE_COACH"));
        request.setClubId(clubId);

        when(userRepository.existsByUsername("coach")).thenReturn(false);
        when(roleRepository.findByName("ROLE_COACH"))
                .thenReturn(Optional.of(new Role("ROLE_COACH")));
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Club not found: " + clubId);
    }

    // endregion

    // region getUserById

    @Test
    void getUserByIdShouldReturnUserWhenExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setRoles(Set.of(new Role("ROLE_ADMIN")));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void getUserByIdShouldThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found: " + userId);
    }

    // endregion

    // region changeOwnPassword

    @Test
    void changeOwnPasswordShouldSucceedWithCorrectOldPassword() {
        UserPrincipal principal = new UserPrincipal("testuser", UUID.randomUUID(), List.of());
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldpass");
        request.setNewPassword("newpass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encoded_old");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "encoded_old")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");

        userService.changeOwnPassword(principal, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded_new");
    }

    @Test
    void changeOwnPasswordShouldThrowWhenOldPasswordIsWrong() {
        UserPrincipal principal = new UserPrincipal("testuser", UUID.randomUUID(), List.of());
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong_old");
        request.setNewPassword("newpass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encoded_old");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_old", "encoded_old")).thenReturn(false);

        assertThatThrownBy(() -> userService.changeOwnPassword(principal, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password is incorrect");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeOwnPasswordShouldThrowWhenPrincipalIsNull() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old");
        request.setNewPassword("new");

        assertThatThrownBy(() -> userService.changeOwnPassword(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not authenticated");
    }

    // endregion

    // region changeUserPasswordAsAdmin

    @Test
    void changeUserPasswordAsAdminShouldSucceed() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newAdminPass");

        User user = new User();
        user.setId(userId);
        user.setUsername("targetuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newAdminPass")).thenReturn("encoded_new_admin");

        userService.changeUserPasswordAsAdmin(userId, request);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encoded_new_admin");
    }

    @Test
    void changeUserPasswordAsAdminShouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newpass");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserPasswordAsAdmin(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found: " + userId);
    }

    // endregion

    // region getCurrentUser

    @Test
    void getCurrentUserShouldReturnUserResponse() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal("testuser", userId, List.of());

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setEnabled(true);
        user.setRoles(Set.of(new Role("ROLE_ADMIN")));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser(principal);

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getRoles()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void getCurrentUserShouldThrowWhenPrincipalIsNull() {
        assertThatThrownBy(() -> userService.getCurrentUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not authenticated");
    }

    @Test
    void getCurrentUserShouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal("testuser", userId, List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found: " + userId);
    }

    // endregion

    // region getAllUsers

    @Test
    void getAllUsersShouldReturnAllForAdmin() {
        UUID adminId = UUID.randomUUID();
        UserPrincipal adminPrincipal = new UserPrincipal("admin", adminId, List.of());

        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setRoles(Set.of(new Role("ROLE_ADMIN")));

        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("coach1");
        user1.setEmail("coach1@test.com");
        user1.setRoles(Set.of(new Role("ROLE_COACH")));

        Page<User> page = new PageImpl<>(List.of(user1));

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(null, null, null, adminPrincipal, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("coach1");
    }

    @Test
    void getAllUsersShouldReturnOnlyClubUsersForClubRole() {
        UUID clubId = UUID.randomUUID();
        UUID clubUserId = UUID.randomUUID();

        Club club = new Club("Test Club");
        club.setId(clubId);

        User clubUser = new User();
        clubUser.setId(clubUserId);
        clubUser.setUsername("club_manager");
        clubUser.setRoles(Set.of(new Role("ROLE_CLUB")));
        clubUser.setClub(club);

        UserPrincipal clubPrincipal = new UserPrincipal("club_manager", clubUserId, List.of());

        User sameClubUser = new User();
        sameClubUser.setId(UUID.randomUUID());
        sameClubUser.setUsername("coach_in_club");
        sameClubUser.setEmail("coach@club.com");
        sameClubUser.setRoles(Set.of(new Role("ROLE_COACH")));
        sameClubUser.setClub(club);

        Page<User> page = new PageImpl<>(List.of(sameClubUser));

        when(userRepository.findById(clubUserId)).thenReturn(Optional.of(clubUser));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(null, null, null, clubPrincipal, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("coach_in_club");
    }

    @Test
    void getAllUsersShouldFilterByUsername() {
        UUID adminId = UUID.randomUUID();
        UserPrincipal adminPrincipal = new UserPrincipal("admin", adminId, List.of());

        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setRoles(Set.of(new Role("ROLE_ADMIN")));

        User matched = new User();
        matched.setId(UUID.randomUUID());
        matched.setUsername("john_doe");
        matched.setRoles(Set.of(new Role("ROLE_COACH")));

        Page<User> page = new PageImpl<>(List.of(matched));

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(null, null, "john", adminPrincipal, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
    }

    @Test
    void getAllUsersShouldFilterByRoleForAdmin() {
        UUID adminId = UUID.randomUUID();
        UserPrincipal adminPrincipal = new UserPrincipal("admin", adminId, List.of());

        User admin = new User();
        admin.setId(adminId);
        admin.setUsername("admin");
        admin.setRoles(Set.of(new Role("ROLE_ADMIN")));

        User coach = new User();
        coach.setId(UUID.randomUUID());
        coach.setUsername("coach_user");
        coach.setRoles(Set.of(new Role("ROLE_COACH")));

        Page<User> page = new PageImpl<>(List.of(coach));

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(null, "ROLE_COACH", null, adminPrincipal, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRoles()).contains("ROLE_COACH");
    }

    @Test
    void getAllUsersShouldReturnEmptyPageForClubRoleWithoutClub() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal("coach_noclub", userId, List.of());

        User user = new User();
        user.setId(userId);
        user.setUsername("coach_noclub");
        user.setRoles(Set.of(new Role("ROLE_COACH")));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Page<UserResponse> result = userService.getAllUsers(null, null, null, principal, PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getAllUsersShouldThrowWhenPrincipalIsNull() {
        assertThatThrownBy(() -> userService.getAllUsers(null, null, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not authenticated");
    }

    // endregion
}
