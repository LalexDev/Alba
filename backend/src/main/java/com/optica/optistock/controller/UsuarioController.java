package com.optica.optistock.controller;

import com.optica.optistock.dto.UsuarioDto;
import com.optica.optistock.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Object list() {
        return usuarioRepository.findAll().stream().map(u -> UsuarioDto.builder()
                .id(u.getId()).nombres(u.getNombres()).apellidos(u.getApellidos()).username(u.getUsername())
                .correo(u.getCorreo()).rol(u.getRole().getNombre()).estado(u.getEstado()).build()).toList();
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        var u = usuarioRepository.findById(id).orElseThrow();
        return UsuarioDto.builder().id(u.getId()).nombres(u.getNombres()).apellidos(u.getApellidos()).username(u.getUsername())
                .correo(u.getCorreo()).rol(u.getRole().getNombre()).estado(u.getEstado()).build();
    }

    @PatchMapping("/{id}/estado")
    public Object toggle(@PathVariable Long id) {
        var u = usuarioRepository.findById(id).orElseThrow();
        u.setEstado(!u.getEstado());
        return usuarioRepository.save(u);
    }

    @PatchMapping("/{id}/password")
    public Object changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var u = usuarioRepository.findById(id).orElseThrow();
        u.setPassword(passwordEncoder.encode(body.get("password")));
        usuarioRepository.save(u);
        return Map.of("message", "Password actualizada");
    }
}
