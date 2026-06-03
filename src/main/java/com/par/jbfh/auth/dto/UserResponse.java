package com.par.jbfh.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private UUID clubId;
    private String clubName;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
}
