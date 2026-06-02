package com.par.jbfh;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Тестовая конфигурация безопасности без JwtAuthenticationFilter.
 * Используется в WebMvc-тестах через @Import(TestSecurityConfig.class).
 * <p>
 * Переопределяет {@link SecurityFilterChain} — без JWT-фильтра,
 * чтобы @WithMockUser работал корректно с @Secured.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }
}