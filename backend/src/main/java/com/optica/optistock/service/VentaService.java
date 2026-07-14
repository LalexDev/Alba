package com.optica.optistock.service;

import com.optica.optistock.dto.VentaRequest;
import com.optica.optistock.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> list();
    Venta get(Long id);
    Venta create(VentaRequest request, String username);
    Venta anular(Long id);
}
