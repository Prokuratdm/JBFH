package com.par.jbfh.team.dto;

import java.util.UUID;

public record CoachResponse(
        UUID id,
        String username,
        String email
) {
}