package com.optica.optistock.service.impl;

import com.optica.optistock.dto.InventarioMovimientoRequest;
import com.optica.optistock.entity.MovimientoInventario;
import com.optica.optistock.entity.TipoMovimiento;
import com.optica.optistock.exception.BadRequestException;
import com.optica.optistock.repository.MovimientoInventarioRepository;
import com.optica.optistock.repository.ProductoRepository;
import com.optica.optistock.repository.UsuarioRepository;
import com.optica.optistock.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<MovimientoInventario> movimientos() {
        return movimientoRepository.findAll();
    }

    @Override
    public MovimientoInventario entrada(InventarioMovimientoRequest request, String username) {
        return mover(request.getProductoId(), request.getCantidad(), TipoMovimiento.ENTRADA_REPOSICION, request.getMotivo(), username);
    }

    @Override
    public MovimientoInventario salida(InventarioMovimientoRequest request, String username) {
        return mover(request.getProductoId(), -request.getCantidad(), TipoMovimiento.SALIDA_PERDIDA, request.getMotivo(), username);
    }

    @Override
    public MovimientoInventario ajuste(Long productoId, Integer nuevoStock, String motivo, String username) {
        var producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new BadRequestException("Producto no encontrado"));
        int anterior = producto.getStockActual();
        producto.setStockActual(nuevoStock);
        productoRepository.save(producto);

        var usuario = usuarioRepository.findByUsernameOrCorreo(username, username).orElse(null);
        return movimientoRepository.save(MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(TipoMovimiento.AJUSTE_MANUAL)
                .cantidad(nuevoStock - anterior)
                .stockAnterior(anterior)
                .stockNuevo(nuevoStock)
                .motivo(motivo)
                .usuario(usuario)
                .build());
    }

    public MovimientoInventario mover(Long productoId, Integer delta, TipoMovimiento tipo, String motivo, String username) {
        var producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new BadRequestException("Producto no encontrado"));
        int anterior = producto.getStockActual();
        int nuevo = anterior + delta;
        if (nuevo < 0) {
            throw new BadRequestException("Stock insuficiente");
        }
        producto.setStockActual(nuevo);
        productoRepository.save(producto);

        var usuario = usuarioRepository.findByUsernameOrCorreo(username, username).orElse(null);
        return movimientoRepository.save(MovimientoInventario.builder()
                .producto(producto)
                .tipoMovimiento(tipo)
                .cantidad(Math.abs(delta))
                .stockAnterior(anterior)
                .stockNuevo(nuevo)
                .motivo(motivo)
                .usuario(usuario)
                .build());
    }
}
