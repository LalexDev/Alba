package com.optica.optistock.service;

import com.optica.optistock.dto.ProductoRequest;
import com.optica.optistock.entity.Producto;

import java.util.List;

public interface ProductoService {
    List<Producto> list();
    Producto get(Long id);
    Producto getByCodigo(String codigo);
    List<Producto> search(String query);
    Producto create(ProductoRequest request);
    Producto update(Long id, ProductoRequest request);
    Producto toggleEstado(Long id);
    List<Producto> bajoStock();
    List<Producto> agotados();
}
