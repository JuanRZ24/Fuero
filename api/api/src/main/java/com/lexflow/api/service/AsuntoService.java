package com.lexflow.api.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.Cliente;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.ClienteRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;


@Service
public class AsuntoService {
    

    private final AsuntoRepository asuntoRepository;
    private final ClienteRepository clienteRepository;
    private final TipoAsuntoRepository tipoAsuntoRepository;

    public AsuntoService (AsuntoRepository asuntoRepository, ClienteRepository clienteRepository, TipoAsuntoRepository tipoAsuntoRepository){
        this.asuntoRepository = asuntoRepository;
        this.clienteRepository = clienteRepository;
        this.tipoAsuntoRepository = tipoAsuntoRepository;
    }


    public List<Asunto> obtenerTodos (){
        return asuntoRepository.findAll();
    }

    public Optional<Asunto> obtenerAsunto(Long id){
        return asuntoRepository.findById(id);
    }

    public Asunto guardarAsunto(Asunto asunto) {
        // 1. Buscamos al cliente completo en la BD usando el ID que nos mandaste
        Cliente cliente = clienteRepository.findById(asunto.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe"));

        // 2. Buscamos el tipo de asunto completo
        TipoAsunto tipo = tipoAsuntoRepository.findById(asunto.getTipoAsunto().getId())
                .orElseThrow(() -> new RuntimeException("Error: El tipo de asunto no existe"));

        // 3. Se los inyectamos al asunto original para que ya no estén en "null"
        asunto.setCliente(cliente);
        asunto.setTipoAsunto(tipo);

        // 4. Guardamos y retornamos (ESTE return es el que le da el ID al Controller)
        return asuntoRepository.save(asunto);
    }

    
    public Optional <Asunto> actualizar(Long id, Asunto AsuntoActualizado){
        return asuntoRepository.findById(id).map(AsuntoExistente -> {
            
            
            if (AsuntoActualizado.getActoImpugnar() != null){
                AsuntoExistente.setActoImpugnar(AsuntoActualizado.getActoImpugnar());
            }

            
            return asuntoRepository.save(AsuntoExistente); 
        });
    }

    public boolean eliminar (Long id){
        asuntoRepository.deleteById(id);
        return true;
    }


}
