package com.lexflow.api.dto;

import com.lexflow.api.model.EstadoDoc;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DocumentoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String googleDocId; // 👈 Clave para React e iFrame
    private String googleDocUrl;
    private EstadoDoc estadoRevision;
    private LocalDateTime fechaSubida;
    private Long asuntoId;
    private String nombreCreador; // Solo el nombre, por seguridad
}