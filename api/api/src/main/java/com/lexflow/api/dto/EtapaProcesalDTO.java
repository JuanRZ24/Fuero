package com.lexflow.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor  // 🔥 ESTA ES LA QUE TE FALTA PARA JACKSON
@AllArgsConstructor // 🔥 ESTA ES NECESARIA PARA QUE @BUILDER NO DE ERROR
public class EtapaProcesalDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private Boolean esEtapaFinal;
    private Long tipoAsuntoId;
}