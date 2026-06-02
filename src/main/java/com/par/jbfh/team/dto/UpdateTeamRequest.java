package com.par.jbfh.team.dto;

import lombok.Data;

@Data
public class UpdateTeamRequest {

    private String name;

    private Integer year;

    private String description;
}