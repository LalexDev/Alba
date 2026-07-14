package com.optica.optistock.controller;

import com.optica.optistock.dto.VentaRequest;
import com.optica.optistock.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;

    @GetMapping
    public Object list() { return ventaService.list(); }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) { return ventaService.get(id); }

    @PostMapping
    public Object create(@RequestBody @Valid VentaRequest request, Authentication authentication) {
        return ventaService.create(request, authentication.getName());
    }

    @PatchMapping("/{id}/anular")
    public Object anular(@PathVariable Long id) { return ventaService.anular(id); }
}
