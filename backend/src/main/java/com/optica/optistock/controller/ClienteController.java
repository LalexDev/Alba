package com.optica.optistock.controller;

import com.optica.optistock.dto.ClienteRequest;
import com.optica.optistock.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping
    public Object list() { return clienteService.list(); }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) { return clienteService.get(id); }

    @GetMapping("/buscar")
    public Object search(@RequestParam String query) { return clienteService.search(query); }

    @PostMapping
    public Object create(@RequestBody @Valid ClienteRequest request) { return clienteService.create(request); }

    @PutMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody @Valid ClienteRequest request) { return clienteService.update(id, request); }

    @PatchMapping("/{id}/estado")
    public Object toggleEstado(@PathVariable Long id) { return clienteService.toggleEstado(id); }
}
