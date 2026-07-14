package com.optica.optistock.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {
    @NotBlank
    private String codigoBarras;
    @NotBlank
    private String nombre;
    private String descripcion;
    private Long categoriaId;
    private Long marcaId;
    private Long proveedorId;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioCompra;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioVenta;
    @NotNull
    @Min(0)
    private Integer stockActual;
    @NotNull
    @Min(0)
    private Integer stockMinimo;
}
