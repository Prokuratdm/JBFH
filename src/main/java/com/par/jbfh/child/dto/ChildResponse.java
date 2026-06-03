package com.par.jbfh.child.dto;

import com.par.jbfh.child.enums.Gender;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChildResponse(
        UUID id,
        String firstName,
        String lastName,
        String middleName,
        int birthYear,
        Gender gender,
        UUID teamId,
        String teamName,
        LocalDateTime createdAt
) {
}