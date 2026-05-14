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

    public List<EtapaProcesal> getAll() {
        return etapaRepository.findAll();
    }

    public List<EtapaProcesal> getByType(Long tipoId) {
        return etapaRepository.findAll().stream()
                .filter(e -> e.getTipoAsunto() != null && e.getTipoAsunto().getId().equals(tipoId))
                .toList();
    }
}