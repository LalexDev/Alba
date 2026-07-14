package com.optica.optistock.repository;

import com.optica.optistock.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsernameOrCorreo(String username, String correo);
    boolean existsByUsername(String username);
    boolean existsByCorreo(String correo);
}
