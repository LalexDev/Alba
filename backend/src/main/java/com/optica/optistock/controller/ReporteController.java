package com.optica.optistock.controller;

import com.optica.optistock.repository.MovimientoInventarioRepository;
import com.optica.optistock.repository.ProductoRepository;
import com.optica.optistock.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    @GetMapping("/dashboard")
    public Object dashboard() {
        var ventas = ventaRepository.findAll();
        BigDecimal total = ventas.stream().map(v -> v.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of(
                "totalVentas", total,
                "cantidadVentas", ventas.size(),
                "totalProductos", productoRepository.count(),
                "movimientosInventario", movimientoRepository.count()
        );
    }

    @GetMapping("/ventas")
    public Object ventas() { return ventaRepository.findAll(); }

    @GetMapping("/bajo-stock")
    public Object bajoStock() { return productoRepository.findAll().stream().filter(p -> p.getStockActual() <= p.getStockMinimo()).toList(); }
}
