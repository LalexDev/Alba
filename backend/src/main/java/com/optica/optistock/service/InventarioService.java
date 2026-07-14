package com.optica.optistock.service;

import com.optica.optistock.dto.InventarioMovimientoRequest;
import com.optica.optistock.entity.MovimientoInventario;

import java.util.List;

public interface InventarioService {
    List<MovimientoInventario> movimientos();
    MovimientoInventario entrada(InventarioMovimientoRequest request, String username);
    MovimientoInventario salida(InventarioMovimientoRequest request, String username);
    MovimientoInventario ajuste(Long productoId, Integer nuevoStock, String motivo, String username);
}
