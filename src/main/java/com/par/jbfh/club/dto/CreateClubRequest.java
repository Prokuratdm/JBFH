package com.par.jbfh.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClubRequest {

    @NotBlank
    private String name;

    private String address;

    private String description;
}
