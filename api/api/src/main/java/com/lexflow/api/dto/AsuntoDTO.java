package com.lexflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    private Long id;
    private String actoImpugnar;
    private String descripcion;
    
    // 🔥 MAGIA JACKSON 1: Este se ignora al guardar, pero se envía al leer (GET)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ClienteDTO cliente;
    
    // 🔥 MAGIA JACKSON 2: Este se ignora al leer, pero se acepta al guardar (POST/PUT)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long clienteId;
    
    // 🔥 Lo que enviamos a React (GET)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private TipoAsuntoDTO tipoAsunto;
    
    // 🔥 Lo que recibimos de React (POST/PUT)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long tipoAsuntoId;

    private Map<String, Object> camposDinamicos;
}