package com.optica.optistock.service.impl;

import com.optica.optistock.dto.ProductoRequest;
import com.optica.optistock.entity.Producto;
import com.optica.optistock.exception.BadRequestException;
import com.optica.optistock.exception.NotFoundException;
import com.optica.optistock.repository.*;
import com.optica.optistock.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProveedorRepository proveedorRepository;

    @Override
    public List<Producto> list() {
        return productoRepository.findAll();
    }

    @Override
    public Producto get(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    @Override
    public Producto getByCodigo(String codigo) {
        return productoRepository.findByCodigoBarras(codigo).orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    @Override
    public List<Producto> search(String query) {
        return productoRepository.findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(query, query);
    }

    @Override
    public Producto create(ProductoRequest request) {
        validarProducto(request, null);
        Producto producto = mapRequest(new Producto(), request);
        producto.setFechaIngreso(LocalDate.now());
        producto.setEstado(true);
        return productoRepository.save(producto);
    }

    @Override
    public Producto update(Long id, ProductoRequest request) {
        Producto producto = get(id);
        validarProducto(request, id);
        return productoRepository.save(mapRequest(producto, request));
    }

    @Override
    public Producto toggleEstado(Long id) {
        Producto p = get(id);
        p.setEstado(!p.getEstado());
        return productoRepository.save(p);
    }

    @Override
    public List<Producto> bajoStock() {
        return productoRepository.findAll().stream().filter(p -> p.getStockActual() <= p.getStockMinimo()).toList();
    }

    @Override
    public List<Producto> agotados() {
        return productoRepository.findByStockActual(0);
    }

    private void validarProducto(ProductoRequest request, Long id) {
        if (request.getPrecioVenta().compareTo(request.getPrecioCompra()) < 0) {
            throw new BadRequestException("El precio de venta debe ser mayor o igual al precio de compra");
        }
        if ((id == null && productoRepository.existsByCodigoBarras(request.getCodigoBarras())) ||
                (id != null && productoRepository.findByCodigoBarras(request.getCodigoBarras()).filter(p -> !p.getId().equals(id)).isPresent())) {
            throw new BadRequestException("Código de barras ya existe");
        }
    }

    private Producto mapRequest(Producto producto, ProductoRequest request) {
        producto.setCodigoBarras(request.getCodigoBarras());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setCategoria(request.getCategoriaId() != null ? categoriaRepository.findById(request.getCategoriaId()).orElse(null) : null);
        producto.setMarca(request.getMarcaId() != null ? marcaRepository.findById(request.getMarcaId()).orElse(null) : null);
        producto.setProveedor(request.getProveedorId() != null ? proveedorRepository.findById(request.getProveedorId()).orElse(null) : null);
        if (producto.getFechaIngreso() == null) {
            producto.setFechaIngreso(LocalDate.now());
        }
        if (producto.getEstado() == null) {
            producto.setEstado(true);
        }
        return producto;
    }
}
