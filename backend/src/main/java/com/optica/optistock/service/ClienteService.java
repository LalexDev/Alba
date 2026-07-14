package com.optica.optistock.service;

import com.optica.optistock.dto.ClienteRequest;
import com.optica.optistock.entity.Cliente;

import java.util.List;

public interface ClienteService {
    List<Cliente> list();
    List<Cliente> search(String query);
    Cliente get(Long id);
    Cliente create(ClienteRequest request);
    Cliente update(Long id, ClienteRequest request);
    Cliente toggleEstado(Long id);
}
