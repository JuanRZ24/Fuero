package com.lexflow.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

public class VencimientoDTO {

    // Lo que nos manda React para CREAR un vencimiento
    @Data
    public static class Request {
        private LocalDate fechaLimite;
        private String descripcion;
        private Long asuntoId;
        private Long etapaId; // Puede venir null
    }

    // Lo que le mandamos a React para MOSTRAR en el Dashboard/Calendario
    @Data
    @Builder
    public static class Response {
        private Long id;
        private LocalDate fechaLimite;
        private String descripcion;
        private boolean completado;
        private Long asuntoId;
        private String asuntoFolio; // Ej. EXP-0004
        private String clienteNombre;
        private String etapaNombre; // Ej. "Demanda" (si tiene)
    }
}