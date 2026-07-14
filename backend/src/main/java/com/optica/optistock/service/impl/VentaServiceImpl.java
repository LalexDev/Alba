package com.optica.optistock.service.impl;

import com.optica.optistock.dto.DetalleVentaRequest;
import com.optica.optistock.dto.VentaRequest;
import com.optica.optistock.entity.*;
import com.optica.optistock.exception.BadRequestException;
import com.optica.optistock.exception.NotFoundException;
import com.optica.optistock.repository.*;
import com.optica.optistock.service.VentaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PagoRepository pagoRepository;
    private final InventarioServiceImpl inventarioService;

    @Override
    public List<Venta> list() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta get(Long id) {
        return ventaRepository.findById(id).orElseThrow(() -> new NotFoundException("Venta no encontrada"));
    }

    @Override
    @Transactional
    public Venta create(VentaRequest request, String username) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new BadRequestException("No se puede confirmar venta vacía");
        }
        Usuario vendedor = usuarioRepository.findByUsernameOrCorreo(username, username)
                .orElseThrow(() -> new NotFoundException("Usuario vendedor no encontrado"));

        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new BadRequestException("Cliente no encontrado"));
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        Venta venta = Venta.builder()
                .numeroVenta(generarNumeroVenta())
                .cliente(cliente)
                .usuarioVendedor(vendedor)
                .metodoPago(request.getMetodoPago())
                .estado(EstadoVenta.REGISTRADA)
                .observaciones(request.getObservaciones())
                .descuentoTotal(request.getDescuentoTotal() == null ? BigDecimal.ZERO : request.getDescuentoTotal())
                .build();

        for (DetalleVentaRequest detalleReq : request.getDetalles()) {
            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new BadRequestException("Producto no encontrado"));
            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new BadRequestException("Producto inactivo");
            }
            if (producto.getStockActual() < detalleReq.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para " + producto.getNombre());
            }

            BigDecimal descuento = detalleReq.getDescuento() == null ? BigDecimal.ZERO : detalleReq.getDescuento();
            BigDecimal linea = producto.getPrecioVenta().multiply(BigDecimal.valueOf(detalleReq.getCantidad())).subtract(descuento);
            subtotal = subtotal.add(linea);

            var detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(detalleReq.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .descuento(descuento)
                    .subtotal(linea)
                    .build();
            venta.getDetalles().add(detalle);
        }

        BigDecimal totalSinIgv = subtotal.subtract(venta.getDescuentoTotal());
        BigDecimal igv = totalSinIgv.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = totalSinIgv.add(igv);

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);

        pagoRepository.save(Pago.builder()
                .venta(guardada)
                .metodoPago(request.getMetodoPago())
                .monto(total)
                .codigoOperacion(request.getCodigoOperacion())
                .entidad(request.getEntidad())
                .build());

        for (DetalleVenta detalle : guardada.getDetalles()) {
            inventarioService.mover(
                    detalle.getProducto().getId(),
                    -detalle.getCantidad(),
                    TipoMovimiento.SALIDA_VENTA,
                    "Salida por venta " + guardada.getNumeroVenta(),
                    username
            );
        }
        return guardada;
    }

    @Override
    public Venta anular(Long id) {
        Venta venta = get(id);
        venta.setEstado(EstadoVenta.ANULADA);
        return ventaRepository.save(venta);
    }

    private String generarNumeroVenta() {
        long next = ventaRepository.findTopByOrderByIdDesc().map(v -> v.getId() + 1).orElse(1L);
        return "VTA-" + String.format("%06d", next);
    }
}
