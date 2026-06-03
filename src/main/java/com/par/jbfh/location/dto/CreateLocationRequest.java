package com.par.jbfh.location.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLocationRequest {

    @NotBlank
    private String name;
}