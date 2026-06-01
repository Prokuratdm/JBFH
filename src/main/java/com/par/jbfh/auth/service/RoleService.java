package com.par.jbfh.auth.service;

import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    private static final Set<String> FIXED_ROLES = Set.of(
            "ROLE_ADMIN",
            "ROLE_CLUB",
            "ROLE_METHODIST",
            "ROLE_CLUB_METHODIST",
            "ROLE_COACH",
            "ROLE_MAIN_COACH"
    );

    @PostConstruct
    @Transactional
    public void initRoles() {
        for (String roleName : FIXED_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        return roleRepository.findAll().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name));
    }

    public Set<String> getFixedRoles() {
        return FIXED_ROLES;
    }
}