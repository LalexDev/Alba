package com.optica.optistock.controller;

import com.optica.optistock.entity.Categoria;
import com.optica.optistock.entity.Marca;
import com.optica.optistock.entity.Proveedor;
import com.optica.optistock.repository.CategoriaRepository;
import com.optica.optistock.repository.MarcaRepository;
import com.optica.optistock.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CatalogoController {
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProveedorRepository proveedorRepository;

    @GetMapping("/api/categorias")
    public Object categorias() { return categoriaRepository.findAll(); }

    @PostMapping("/api/categorias")
    public Object crearCategoria(@RequestBody Categoria categoria) { categoria.setId(null); return categoriaRepository.save(categoria); }

    @PutMapping("/api/categorias/{id}")
    public Object editarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) { categoria.setId(id); return categoriaRepository.save(categoria); }

    @PatchMapping("/api/categorias/{id}/estado")
    public Object estadoCategoria(@PathVariable Long id) { var c = categoriaRepository.findById(id).orElseThrow(); c.setEstado(!c.getEstado()); return categoriaRepository.save(c); }

    @GetMapping("/api/marcas")
    public Object marcas() { return marcaRepository.findAll(); }

    @PostMapping("/api/marcas")
    public Object crearMarca(@RequestBody Marca marca) { marca.setId(null); return marcaRepository.save(marca); }

    @PutMapping("/api/marcas/{id}")
    public Object editarMarca(@PathVariable Long id, @RequestBody Marca marca) { marca.setId(id); return marcaRepository.save(marca); }

    @PatchMapping("/api/marcas/{id}/estado")
    public Object estadoMarca(@PathVariable Long id) { var m = marcaRepository.findById(id).orElseThrow(); m.setEstado(!m.getEstado()); return marcaRepository.save(m); }

    @GetMapping("/api/proveedores")
    public Object proveedores() { return proveedorRepository.findAll(); }

    @PostMapping("/api/proveedores")
    public Object crearProveedor(@RequestBody Proveedor proveedor) { proveedor.setId(null); return proveedorRepository.save(proveedor); }

    @PutMapping("/api/proveedores/{id}")
    public Object editarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) { proveedor.setId(id); return proveedorRepository.save(proveedor); }

    @PatchMapping("/api/proveedores/{id}/estado")
    public Object estadoProveedor(@PathVariable Long id) { var p = proveedorRepository.findById(id).orElseThrow(); p.setEstado(!p.getEstado()); return proveedorRepository.save(p); }
}
