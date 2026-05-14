package com.lexflow.api.service;

import com.lexflow.api.dto.CampoPlantillaDTO;
import com.lexflow.api.dto.PlantillaDTO;
import com.lexflow.api.model.CampoPlantilla;
import com.lexflow.api.model.Plantilla;
import com.lexflow.api.repository.PlantillaRepository;
import com.lexflow.api.security.TenantContext; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;

    @Transactional
    public PlantillaDTO createPlantilla(PlantillaDTO dto) {
        
        Plantilla plantilla = Plantilla.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .despachoId(TenantContext.getCurrentTenant()) 
                .build();

        if (dto.getCampos() != null) {
            List<CampoPlantilla> campos = new java.util.ArrayList<>();
            
            for (CampoPlantillaDTO c : dto.getCampos()) {
                CampoPlantilla campo = CampoPlantilla.builder()
                        .nombreLabel(c.getNombreLabel())
                        .nombreKey(c.getNombreKey())
                        .tipo(c.getTipo())
                        .requerido(c.isRequerido()) 
                        .plantilla(plantilla)
                        .build();
                        
                campos.add(campo);
            }
            
            plantilla.setCampos(campos);
        }

        Plantilla savedPlantilla = plantillaRepository.save(plantilla);

        dto.setId(savedPlantilla.getId());
        return dto; 
    }



    @Transactional(readOnly = true)
public List<PlantillaDTO> getPlantillasByDespacho() {
    
    return plantillaRepository.findAll().stream()
        .map(p -> PlantillaDTO.builder()
            .id(p.getId())
            .nombre(p.getNombre())
            .descripcion(p.getDescripcion())
            .build())
        .collect(Collectors.toList());
}

@Transactional(readOnly = true)
    public PlantillaDTO getById(Long id) {
        Plantilla plantilla = plantillaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada"));

        List<CampoPlantillaDTO> camposDTO = plantilla.getCampos().stream()
                .map(this::convertToCampoDTO)
                .collect(Collectors.toList());

        return PlantillaDTO.builder()
                .id(plantilla.getId())
                .nombre(plantilla.getNombre())
                .descripcion(plantilla.getDescripcion())
                .campos(camposDTO) 
                .build();
    }

    private CampoPlantillaDTO convertToCampoDTO(CampoPlantilla c) {
        return CampoPlantillaDTO.builder()
                .id(c.getId())
                .nombreLabel(c.getNombreLabel())
                .nombreKey(c.getNombreKey())
                .tipo(c.getTipo()) 
                .build();
    }
}