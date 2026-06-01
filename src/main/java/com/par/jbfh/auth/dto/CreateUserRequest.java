package com.par.jbfh.auth.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private Set<String> roles;
    private UUID clubId;
}