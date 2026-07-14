package com.optica.optistock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequest {
    @NotBlank
    private String nombres;
    @NotBlank
    private String apellidos;
    @NotBlank
    @Size(min = 8, max = 20)
    private String dni;
    @Size(max = 20)
    private String telefono;
    private String direccion;
    @Email
    private String correo;
    private String observaciones;
}
