package com.lexflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<CampoPlantillaDTO> campos;
}