package com.lexflow.api.service;

import com.lexflow.api.dto.EtapaProcesalDTO;
import com.lexflow.api.model.EtapaProcesal;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.repository.EtapaProcesalRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EtapaProcesalService {

    private final EtapaProcesalRepository etapaRepository;
    private final TipoAsuntoRepository tipoAsuntoRepository;

    @Transactional(readOnly = true)
    public List<EtapaProcesalDTO> obtenerEtapasPorTipoAsunto(Long tipoAsuntoId) {
        // Usamos el método mágico que creamos en el Repository
        List<EtapaProcesal> etapas = etapaRepository.findByTipoAsuntoIdOrderByOrdenAsc(tipoAsuntoId);

        // Transformamos la lista de Entidades a DTOs
        return etapas.stream().map(etapa -> EtapaProcesalDTO.builder()
                .id(etapa.getId())
                .nombre(etapa.getNombre())
                .orden(etapa.getOrden())
                .esEtapaFinal(etapa.getEsEtapaFinal())
                .build()
        ).toList();
    }


    @Transactional
public EtapaProcesalDTO guardarEtapa(EtapaProcesalDTO dto) {
    // 1. Buscamos el tipo de asunto real
    TipoAsunto tipo = tipoAsuntoRepository.findById(dto.getTipoAsuntoId())
            .orElseThrow(() -> new RuntimeException("Tipo de Asunto no encontrado"));

    // 2. Convertimos DTO a Entidad
    EtapaProcesal nuevaEtapa = EtapaProcesal.builder()
            .nombre(dto.getNombre())
            .orden(dto.getOrden())
            .esEtapaFinal(dto.getEsEtapaFinal())
            .tipoAsunto(tipo)
            .build();

    // 3. Guardamos y regresamos el DTO
    EtapaProcesal guardada = etapaRepository.save(nuevaEtapa);
    
    return EtapaProcesalDTO.builder()
            .id(guardada.getId())
            .nombre(guardada.getNombre())
            .orden(guardada.getOrden())
            .esEtapaFinal(guardada.getEsEtapaFinal())
            .tipoAsuntoId(guardada.getTipoAsunto().getId())
            .build();
}
}