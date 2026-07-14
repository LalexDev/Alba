package com.optica.optistock.dto;

import com.optica.optistock.entity.RoleNombre;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioDto {
    private Long id;
    private String nombres;
    private String apellidos;
    private String username;
    private String correo;
    private RoleNombre rol;
    private Boolean estado;
}
