package com.optica.optistock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventarioMovimientoRequest {
    @NotNull
    private Long productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    private String motivo;
}
