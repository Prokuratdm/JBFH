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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UserPrincipal creator) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        // Assign roles
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        // Handle club assignment
        boolean needsClub = roles.stream().anyMatch(r -> !isRoleWithoutClub(r.getName()));

        if (needsClub) {
            UUID clubId = request.getClubId();
            if (clubId == null && creator != null) {
                // Try to get club from creator if creator is a club user
                clubId = getCreatorClubId(creator);
            }
            if (clubId == null) {
                throw new IllegalArgumentException("Club is required for this role");
            }
            UUID finalClubId = clubId;
            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("Club not found: " + finalClubId));
            user.setClub(club);
        }

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    private boolean isRoleWithoutClub(String roleName) {
        return roleName.equals("ROLE_ADMIN") || roleName.equals("ROLE_METHODIST");
    }

    private UUID getCreatorClubId(UserPrincipal creator) {
        if (creator == null) return null;
        return userRepository.findByUsername(creator.getUsername())
                .map(User::getClub)
                .map(Club::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toUserResponse(user);
    }

    /**
     * Смена пароля самим пользователем. Требует указания старого пароля.
     */
    @Transactional
    public void changeOwnPassword(UserPrincipal principal, ChangePasswordRequest request) {
        if (principal == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUsername()));

        if (request.getOldPassword() == null || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUserId()));
        return toUserResponse(user);
    }

    /**
     * Административная смена пароля любому пользователю. Старый пароль не требуется.
     */
    @Transactional
    public void changeUserPasswordAsAdmin(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Получение списка пользователей с фильтрацией и пагинацией.
     * <p>
     * ADMIN/METHODIST видят всех пользователей, могут фильтровать по clubId и role.
     * Клубные роли (CLUB, CLUB_METHODIST, COACH, MAIN_COACH) видят только пользователей своего клуба.
     * Фильтр username работает для всех ролей (поиск по подстрокам).
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(UUID clubId, String role, String username,
                                          UserPrincipal principal, Pageable pageable) {
        User currentUser = getCurrentUserByPrincipal(principal);
        boolean isAdminOrMethodist = hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_METHODIST");

        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        // Фильтр по username (LIKE %value%)
        if (username != null && !username.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        // Видимость: не-admins видят только пользователей своего клуба
        if (!isAdminOrMethodist) {
            UUID userClubId = currentUser.getClub() != null ? currentUser.getClub().getId() : null;
            if (userClubId == null) {
                return Page.empty(pageable);
            }
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("club").get("id"), userClubId));
        } else {
            // Админ/методист: опциональный фильтр по клубу
            if (clubId != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("club").get("id"), clubId));
            }
        }

        // Фильтр по роли (только для ADMIN/METHODIST)
        if (isAdminOrMethodist && role != null && !role.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                return cb.equal(rolesJoin.get("name"), role);
            });
        }

        return userRepository.findAll(spec, pageable).map(this::toUserResponse);
    }

    private User getCurrentUserByPrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getUserId()));
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getClub() != null ? user.getClub().getId() : null,
                user.getClub() != null ? user.getClub().getName() : null,
                user.getLastSeenAt(),
                user.getCreatedAt()
        );
    }
}
