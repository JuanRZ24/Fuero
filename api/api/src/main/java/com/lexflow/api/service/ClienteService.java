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



    public List<Cliente> obtenerTodos(){
        return clienteRepository.findAll();
    }

    public Optional <Cliente> obtenerPorId(Long id){
        return clienteRepository.findById(id);
    }

    public Cliente guardar(Cliente cliente){
        cliente.setDespachoId(TenantContext.getCurrentTenant());
        return clienteRepository.save(cliente);
    }

    public Optional <Cliente> actualizar(Long id, Cliente clienteActualizado){
        return clienteRepository.findById(id).map(clienteExistente -> {
            
            
            if (clienteActualizado.getNombre() != null){
                clienteExistente.setNombre(clienteActualizado.getNombre());
            }

            
            return clienteRepository.save(clienteExistente); 
        });
    }

    public boolean eliminar (Long id){
        clienteRepository.deleteById(id);
        return true;
    }


}
