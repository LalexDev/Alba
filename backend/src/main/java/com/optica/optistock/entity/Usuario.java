package com.optica.optistock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol")
    private Role role;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "email", nullable = false, unique = true)
    private String correo;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "activo", nullable = false)
    private Boolean estado = true;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime fechaActualizacion;
}