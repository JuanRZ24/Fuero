package com.lexflow.api.service;

import com.lexflow.api.model.EtapaProcesal;
import com.lexflow.api.repository.EtapaProcesalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtapaProcesalService {

    private final EtapaProcesalRepository etapaRepository;

    public List<EtapaProcesal> obtenerTodas() {
        return etapaRepository.findAll();
    }

    public List<EtapaProcesal> obtenerPorTipo(Long tipoId) {
        // Filtramos la lista para devolver solo las del tipo de asunto correspondiente
        return etapaRepository.findAll().stream()
                .filter(e -> e.getTipoAsunto() != null && e.getTipoAsunto().getId().equals(tipoId))
                .toList();
    }
}