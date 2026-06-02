package com.par.jbfh.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("Bad input"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Bad input");
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithLongMessage() {
        ProblemDetail problem = handler.handleIllegalArgument(
                new IllegalArgumentException("Username already exists: testuser"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Username already exists: testuser");
    }

    @Test
    void shouldHandleAccessDeniedException() {
        ProblemDetail problem = handler.handleAccessDenied(
                new AccessDeniedException("Access denied"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problem.getDetail()).isEqualTo("Access denied");
        assertThat(problem.getTitle()).isEqualTo("Forbidden");
    }

    @Test
    void shouldHandleGeneralException() {
        ProblemDetail problem = handler.handleGeneral(new RuntimeException("Unexpected error"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualTo("Internal server error");
        assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
    }
}