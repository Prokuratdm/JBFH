package com.par.jbfh.club.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClubRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String address;

    private String description;
}