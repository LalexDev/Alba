package com.optica.optistock.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String usernameOrCorreo;

    @NotBlank
    private String password;
}
