package com.optica.optistock.controller;

import com.optica.optistock.dto.ProductoRequest;
import com.optica.optistock.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @GetMapping
    public Object list() { return productoService.list(); }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) { return productoService.get(id); }

    @GetMapping("/codigo/{codigoBarras}")
    public Object getByCodigo(@PathVariable String codigoBarras) { return productoService.getByCodigo(codigoBarras); }

    @GetMapping("/buscar")
    public Object search(@RequestParam String query) { return productoService.search(query); }

    @PostMapping
    public Object create(@RequestBody @Valid ProductoRequest request) { return productoService.create(request); }

    @PutMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody @Valid ProductoRequest request) { return productoService.update(id, request); }

    @PatchMapping("/{id}/estado")
    public Object toggle(@PathVariable Long id) { return productoService.toggleEstado(id); }

    @GetMapping("/bajo-stock")
    public Object bajoStock() { return productoService.bajoStock(); }

    @GetMapping("/agotados")
    public Object agotados() { return productoService.agotados(); }
}
