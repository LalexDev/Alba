package com.optica.optistock.controller;

import com.optica.optistock.entity.ConfiguracionOptica;
import com.optica.optistock.repository.ConfiguracionOpticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {
    private final ConfiguracionOpticaRepository configuracionRepository;

    @GetMapping
    public Object get() {
        return configuracionRepository.findAll().stream().findFirst().orElse(null);
    }

    @PutMapping
    public Object update(@RequestBody ConfiguracionOptica configuracion) {
        ConfiguracionOptica existing = configuracionRepository.findAll().stream().findFirst().orElse(null);
        if (existing != null) {
            configuracion.setId(existing.getId());
        }
        return configuracionRepository.save(configuracion);
    }
}
