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


@Service
@RequiredArgsConstructor
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;

    @Transactional
    public PlantillaDTO crearPlantilla(PlantillaDTO dto) {
        
        // 1. Creamos la cabecera de la plantilla
        Plantilla plantilla = Plantilla.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .despachoId(TenantContext.getCurrentTenant()) // ¡Candado de seguridad!
                .build();

        // 2. Convertimos los campos del DTO a Entidades y los enlazamos a la plantilla
        if (dto.getCampos() != null) {
            List<CampoPlantilla> campos = new java.util.ArrayList<>();
            
            for (CampoPlantillaDTO c : dto.getCampos()) {
                CampoPlantilla campo = CampoPlantilla.builder()
                        .nombreLabel(c.getNombreLabel())
                        .nombreKey(c.getNombreKey())
                        .tipo(c.getTipo())
                        .requerido(c.isRequerido()) // 🚨 Si marca rojo aquí, cámbialo por c.getRequerido()
                        .plantilla(plantilla)
                        .build();
                        
                campos.add(campo);
            }
            
            plantilla.setCampos(campos);
        }

        // 3. Guardamos todo de un solo golpe (gracias al CascadeType.ALL)
        Plantilla plantillaGuardada = plantillaRepository.save(plantilla);

        // 4. Devolvemos el DTO con el ID autogenerado para confirmar
        dto.setId(plantillaGuardada.getId());
        return dto; 
    }
}