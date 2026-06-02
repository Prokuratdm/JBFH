package com.par.jbfh.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInventoryRequest {

    @NotBlank
    private String name;

    private UUID clubId;
}