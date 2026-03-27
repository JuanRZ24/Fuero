package com.lexflow.api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TareaRequestDTO {
    private String titulo;
    private String descripcion;
    private LocalDate fechaLimite;
    private Long asuntoId; 
    private Long usuarioAsignadoId; 
}