package com.lexflow.api.dto;

import com.lexflow.api.model.TipoCampo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampoPlantillaDTO {
    private Long id;
    private String nombreLabel; // Lo que lee el usuario: "¿Monto demandado?"
    private String nombreKey;   // La llave para el JSONB: "monto_demandado"
    private TipoCampo tipo;     // TEXTO, NUMERO, BOOLEANO...
    private boolean requerido;
}