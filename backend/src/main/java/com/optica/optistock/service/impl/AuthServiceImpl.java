package com.optica.optistock.service.impl;

import com.optica.optistock.dto.*;
import com.optica.optistock.entity.RoleNombre;
import com.optica.optistock.entity.Usuario;
import com.optica.optistock.exception.BadRequestException;
import com.optica.optistock.exception.NotFoundException;
import com.optica.optistock.repository.RoleRepository;
import com.optica.optistock.repository.UsuarioRepository;
import com.optica.optistock.security.JwtService;
import com.optica.optistock.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsernameOrCorreo(), request.getPassword()));
        Usuario usuario = usuarioRepository.findByUsernameOrCorreo(request.getUsernameOrCorreo(), request.getUsernameOrCorreo())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRole().getNombre().name())
                .build());

        return LoginResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .correo(usuario.getCorreo())
                .role(usuario.getRole().getNombre().name())
                .build();
    }

    @Override
    public UsuarioDto registerAdmin(RegisterAdminRequest request) {
        if (usuarioRepository.findAll().stream().anyMatch(u -> u.getRole().getNombre() == RoleNombre.ADMINISTRADOR)) {
            throw new BadRequestException("Ya existe un administrador registrado");
        }
        if (usuarioRepository.existsByUsername(request.getUsername()) || usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("Username o correo ya registrado");
        }

        var role = roleRepository.findByNombre(RoleNombre.ADMINISTRADOR)
                .orElseThrow(() -> new NotFoundException("Rol Administrador no encontrado"));

        var user = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .username(request.getUsername())
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .estado(true)
                .build();

        return toDto(usuarioRepository.save(user));
    }

    @Override
    public UsuarioDto me(String username) {
        return toDto(usuarioRepository.findByUsernameOrCorreo(username, username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado")));
    }

    private UsuarioDto toDto(Usuario usuario) {
        return UsuarioDto.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .username(usuario.getUsername())
                .correo(usuario.getCorreo())
                .rol(usuario.getRole().getNombre())
                .estado(usuario.getEstado())
                .build();
    }
}
