package com.optica.optistock.service.impl;

import com.optica.optistock.dto.ClienteRequest;
import com.optica.optistock.entity.Cliente;
import com.optica.optistock.exception.BadRequestException;
import com.optica.optistock.exception.NotFoundException;
import com.optica.optistock.repository.ClienteRepository;
import com.optica.optistock.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;

    @Override
    public List<Cliente> list() {
        return clienteRepository.findAll();
    }

    @Override
    public List<Cliente> search(String query) {
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContainingIgnoreCaseOrTelefonoContainingIgnoreCase(
                query, query, query, query
        );
    }

    @Override
    public Cliente get(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    @Override
    public Cliente create(ClienteRequest request) {
        clienteRepository.findByDni(request.getDni()).ifPresent(c -> {
            throw new BadRequestException("DNI ya registrado");
        });

        Cliente cliente = Cliente.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .dni(request.getDni())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .correo(request.getCorreo())
                .observaciones(request.getObservaciones())
                .estado(true)
                .build();
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Long id, ClienteRequest request) {
        Cliente cliente = get(id);
        if (!cliente.getDni().equals(request.getDni())) {
            clienteRepository.findByDni(request.getDni()).ifPresent(c -> {
                throw new BadRequestException("DNI ya registrado");
            });
        }
        cliente.setNombres(request.getNombres());
        cliente.setApellidos(request.getApellidos());
        cliente.setDni(request.getDni());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setCorreo(request.getCorreo());
        cliente.setObservaciones(request.getObservaciones());
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente toggleEstado(Long id) {
        Cliente cliente = get(id);
        cliente.setEstado(!cliente.getEstado());
        return clienteRepository.save(cliente);
    }
}
