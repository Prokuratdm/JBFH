package com.par.jbfh;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Базовый класс для всех WebMvc-тестов контроллеров.
 * <p>
 * Переопределяет {@link SecurityFilterChain} без JwtAuthenticationFilter,
 * чтобы @WithMockUser работал корректно вместе с @Secured аннотациями.
 */
public abstract class AbstractWebMvcTest {

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

            return http.build();
        }
    }
}