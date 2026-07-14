package com.optica.optistock.dto;

import com.optica.optistock.entity.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VentaRequest {
    private Long clienteId;
    @NotNull
    private MetodoPago metodoPago;
    private String codigoOperacion;
    private String entidad;
    private String observaciones;
    private BigDecimal descuentoTotal = BigDecimal.ZERO;
    @Valid
    @NotEmpty
    private List<DetalleVentaRequest> detalles;
}
