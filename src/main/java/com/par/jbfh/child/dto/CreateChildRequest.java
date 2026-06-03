package com.par.jbfh.child.dto;

import com.par.jbfh.child.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChildRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String middleName;

    @NotNull
    private Integer birthYear;

    @NotNull
    private Gender gender;
}