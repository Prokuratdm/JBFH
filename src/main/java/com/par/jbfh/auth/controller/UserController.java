package com.par.jbfh.auth.controller;

import com.par.jbfh.auth.dto.CreateUserRequest;
import com.par.jbfh.auth.dto.UserResponse;
import com.par.jbfh.auth.service.UserService;
import com.par.jbfh.config.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Get user by ID", description = "Returns user details. Admin only.")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }
}