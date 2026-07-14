package com.optica.optistock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaRequest {
    @NotNull
    private Long productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    private BigDecimal descuento = BigDecimal.ZERO;
}
