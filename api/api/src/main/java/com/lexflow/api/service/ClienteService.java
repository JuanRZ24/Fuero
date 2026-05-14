package com.lexflow.api.service;

import org.springframework.stereotype.Service;

import com.lexflow.api.repository.ClienteRepository;
import com.lexflow.api.security.TenantContext;
import com.lexflow.api.model.Cliente;


import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    

    private final ClienteRepository clienteRepository;

    public ClienteService (ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }



    public List<Cliente> getAll(){
        return clienteRepository.findAll();
    }

    public Optional <Cliente> getById(Long id){
        return clienteRepository.findById(id);
    }

    public Cliente save(Cliente cliente){
        cliente.setDespachoId(TenantContext.getCurrentTenant());
        return clienteRepository.save(cliente);
    }

    public Optional <Cliente> update(Long id, Cliente updatedCliente){
        return clienteRepository.findById(id).map(existingCliente -> {
            
            if (updatedCliente.getNombre() != null){
                existingCliente.setNombre(updatedCliente.getNombre());
            }

            return clienteRepository.save(existingCliente); 
        });
    }

    public boolean delete(Long id){
        clienteRepository.deleteById(id);
        return true;
    }


}