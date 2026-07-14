package com.optica.optistock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "configuracion_optica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionOptica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_optica", nullable = false)
    private String nombreOptica;

    private String ruc;

    private String direccion;

    private String telefono;

    private String correo;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "mensaje_comprobante")
    private String mensajeComprobante;

    @Column(nullable = false)
    private String moneda;

    @Column(name = "igv_activo", nullable = false)
    private Boolean igvActivo;

    @Column(name = "porcentaje_igv", nullable = false)
    private BigDecimal porcentajeIgv;
}
