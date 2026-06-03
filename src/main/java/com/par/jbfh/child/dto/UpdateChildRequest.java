package com.par.jbfh.child.dto;

import com.par.jbfh.child.enums.Gender;
import lombok.Data;

@Data
public class UpdateChildRequest {

    private String firstName;

    private String lastName;

    private String middleName;

    private Integer birthYear;

    private Gender gender;
}