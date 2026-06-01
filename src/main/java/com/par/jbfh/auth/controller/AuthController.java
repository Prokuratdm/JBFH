package com.par.jbfh.auth.controller;

import com.par.jbfh.auth.dto.LoginRequest;
import com.par.jbfh.auth.dto.LoginResponse;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.UserRepository;
import com.par.jbfh.config.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("User is disabled");
        }

        var roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        String token = jwtService.generateToken(user.getUsername(), user.getId(), roles);

        return ResponseEntity.ok(new LoginResponse(token, user.getId(), user.getUsername(), roles));
    }
}