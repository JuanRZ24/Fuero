package com.lexflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsuntoDTO {
    
    private Long id; // Lo usamos cuando devolvemos la info al frontend
    
    private String titulo;
    
    private String descripcion;
    
    // IDs planos para las relaciones (¡Mucho más fácil para React!)
    private Long clienteId;
    private Long tipoAsuntoId;
    
    // 🔥 Nuestro cajón mágico
    private Map<String, Object> camposDinamicos;
}