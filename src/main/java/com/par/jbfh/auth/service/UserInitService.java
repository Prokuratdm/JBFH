package com.par.jbfh.auth.service;

import com.par.jbfh.auth.entity.Role;
import com.par.jbfh.auth.entity.User;
import com.par.jbfh.auth.repository.RoleRepository;
import com.par.jbfh.auth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInitService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initDefaultAdmin() {
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Default admin user already exists");
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found. Ensure RoleService.initRoles() runs first."));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@jbfh.by");
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
        log.info("Default admin user created: admin / admin123");
    }
}