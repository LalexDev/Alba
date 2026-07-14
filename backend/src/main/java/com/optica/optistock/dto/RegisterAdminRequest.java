package com.optica.optistock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterAdminRequest {
    @NotBlank
    private String nombres;
    @NotBlank
    private String apellidos;
    @NotBlank
    private String username;
    @Email
    @NotBlank
    private String correo;
    @NotBlank
    private String password;
}
