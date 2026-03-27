package com.lexflow.api.service;

import com.lexflow.api.repository.TipoAsuntoRepository;
import com.lexflow.api.model.TipoAsunto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TipoAsuntoService {

    private final TipoAsuntoRepository tipoAsuntoRepository;

    public TipoAsuntoService(TipoAsuntoRepository tipoAsuntoRepository) {
        this.tipoAsuntoRepository = tipoAsuntoRepository;
    }

    
    public List<TipoAsunto> getAllTipoAsunto() {
        return tipoAsuntoRepository.findAll();
    }

    public TipoAsunto getTipoAsuntoById(Long id) {
        return tipoAsuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo Asunto not found with id: " + id));
    }

    public TipoAsunto createTipoAsunto(TipoAsunto tipoAsunto) {
        return tipoAsuntoRepository.save(tipoAsunto);
    }

    public TipoAsunto updateTipoAsunto(Long id, TipoAsunto tipoAsuntoDetails) {
        TipoAsunto tipoAsunto = tipoAsuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo Asunto not found with id: " + id));

        tipoAsunto.setNombre(tipoAsuntoDetails.getNombre());
        tipoAsunto.setDescripcion(tipoAsuntoDetails.getDescripcion());

        return tipoAsuntoRepository.save(tipoAsunto);
    }

    public void deleteTipoAsunto(Long id) {
        tipoAsuntoRepository.deleteById(id);
    }

}