package com.lexflow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MovimientoProcesalDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaMovimiento;
    private LocalDate fechaVencimiento;
    
    // Solo mandamos nombres e IDs, no los objetos completos
    private Long asuntoId;
    private String nombreCreador; // Para saber qué abogado lo hizo
    private Long documentoId; // Por si hay un PDF adjunto
    private String nombreDocumento; 
}