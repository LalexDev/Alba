package com.optica.optistock.repository;

import com.optica.optistock.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    boolean existsByCodigoBarras(String codigoBarras);
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String nombre, String codigo);
    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);
    List<Producto> findByStockActual(Integer stockActual);
}
