package com.optica.optistock.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Object dashboard() {
        BigDecimal totalVentas = (BigDecimal) entityManager
                .createNativeQuery("""
                    SELECT COALESCE(SUM(total), 0)
                    FROM ventas
                    WHERE estado_venta = 'REGISTRADA'
                """)
                .getSingleResult();

        Long cantidadVentas = ((Number) entityManager
                .createNativeQuery("""
                    SELECT COUNT(*)
                    FROM ventas
                    WHERE estado_venta = 'REGISTRADA'
                """)
                .getSingleResult()).longValue();

        Long totalProductos = ((Number) entityManager
                .createNativeQuery("""
                    SELECT COUNT(*)
                    FROM productos
                    WHERE activo = TRUE
                """)
                .getSingleResult()).longValue();

        Long bajoStock = ((Number) entityManager
                .createNativeQuery("""
                    SELECT COUNT(*)
                    FROM productos
                    WHERE activo = TRUE
                    AND stock_actual <= stock_minimo
                """)
                .getSingleResult()).longValue();

        Long movimientosInventario = ((Number) entityManager
                .createNativeQuery("""
                    SELECT COUNT(*)
                    FROM movimientos_inventario
                """)
                .getSingleResult()).longValue();

        return Map.of(
                "totalVentas", totalVentas,
                "cantidadVentas", cantidadVentas,
                "totalProductos", totalProductos,
                "bajoStock", bajoStock,
                "movimientosInventario", movimientosInventario
        );
    }

    @GetMapping("/ventas-diarias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Object ventasDiarias() {
        return entityManager
                .createNativeQuery("""
                    SELECT fecha, cantidad_ventas, total_vendido, total_efectivo, total_yape, total_transferencia
                    FROM vw_ventas_diarias
                    ORDER BY fecha DESC
                """)
                .getResultList();
    }

    @GetMapping("/productos-mas-vendidos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Object productosMasVendidos() {
        return entityManager
                .createNativeQuery("""
                    SELECT id_producto, codigo_interno, nombre, unidades_vendidas, importe_vendido
                    FROM vw_productos_mas_vendidos
                    ORDER BY unidades_vendidas DESC
                    LIMIT 20
                """)
                .getResultList();
    }

    @GetMapping("/bajo-stock")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Object bajoStock() {
        return entityManager
                .createNativeQuery("""
                    SELECT id_producto, codigo_interno, codigo_barras, nombre, categoria, marca, stock_actual, stock_minimo, precio_venta
                    FROM vw_productos_stock_bajo
                    ORDER BY stock_actual ASC
                """)
                .getResultList();
    }
}