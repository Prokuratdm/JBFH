package com.par.jbfh.auth.controller;

import com.par.jbfh.auth.dto.ChangePasswordRequest;
import com.par.jbfh.auth.dto.CreateUserRequest;
import com.par.jbfh.auth.dto.UserResponse;
import com.par.jbfh.auth.service.UserService;
import com.par.jbfh.config.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users management API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_CLUB"})
    @Operation(summary = "Create user", description = "Create a new user. Admin can create any user, Club can only create users for their club.")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return userService.createUser(request, principal);
    }

    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_METHODIST", "ROLE_CLUB", "ROLE_CLUB_METHODIST", "ROLE_COACH", "ROLE_MAIN_COACH"})
    @Operation(summary = "Get all users", description = """
            Returns a paginated list of users.
            Admin/Methodist see all users, can filter by clubId and role.
            Club roles see only users of their club.
            All roles can filter by username (substring search).""")
    public Page<UserResponse> getAllUsers(
            @RequestParam(required = false) UUID clubId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        return userService.getAllUsers(clubId, role, username, principal, pageable);
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Get user by ID", description = "Returns user details. Admin only.")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change own password", description = "Change password for the currently authenticated user. Requires old password.")
    public ResponseEntity<Map<String, String>> changeOwnPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.changeOwnPassword(principal, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping("/{id}/password")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Change user password (Admin)", description = "Admin can change password for any user. Old password is not required.")
    public ResponseEntity<Map<String, String>> changeUserPasswordAsAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeUserPasswordAsAdmin(id, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully for user: " + id));
    }
}
