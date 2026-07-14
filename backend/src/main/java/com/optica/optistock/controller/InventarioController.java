package com.optica.optistock.controller;

import com.optica.optistock.dto.InventarioMovimientoRequest;
import com.optica.optistock.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    @GetMapping("/movimientos")
    public Object movimientos() { return inventarioService.movimientos(); }

    @PostMapping("/entrada")
    public Object entrada(@RequestBody @Valid InventarioMovimientoRequest request, Authentication authentication) {
        return inventarioService.entrada(request, authentication.getName());
    }

    @PostMapping("/salida")
    public Object salida(@RequestBody @Valid InventarioMovimientoRequest request, Authentication authentication) {
        return inventarioService.salida(request, authentication.getName());
    }

    @PostMapping("/ajuste")
    public Object ajuste(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long productoId = Long.valueOf(body.get("productoId").toString());
        Integer nuevoStock = Integer.valueOf(body.get("nuevoStock").toString());
        String motivo = body.getOrDefault("motivo", "Ajuste manual").toString();
        return inventarioService.ajuste(productoId, nuevoStock, motivo, authentication.getName());
    }
}
