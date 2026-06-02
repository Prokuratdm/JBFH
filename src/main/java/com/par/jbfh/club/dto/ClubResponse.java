package com.par.jbfh.club.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ClubResponse {

    private UUID id;
    private String name;
    private String address;
    private String description;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}