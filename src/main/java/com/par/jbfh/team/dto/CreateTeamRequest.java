package com.par.jbfh.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTeamRequest {

    @NotBlank
    private String name;

    @NotNull
    private Integer year;

    private String description;
}