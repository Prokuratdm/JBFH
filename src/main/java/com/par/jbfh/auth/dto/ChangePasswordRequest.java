package com.par.jbfh.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    /**
     * Старый пароль. Обязателен при смене пароля самим пользователем.
     * Может быть null при административной смене пароля.
     */
    private String oldPassword;

    /**
     * Новый пароль. Обязателен в любом случае.
     */
    @NotBlank
    private String newPassword;
}