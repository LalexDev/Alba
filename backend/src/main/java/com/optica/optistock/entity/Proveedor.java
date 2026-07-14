package com.optica.optistock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(unique = true)
    private String ruc;

    private String telefono;

    private String direccion;

    private String correo;

    private String contacto;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false)
    private Boolean estado = true;
}
