package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_estados_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @TenantId
    @Column(name = "despacho_id", nullable = false, updatable = false)
    private Long  despachoId;   // usa el MISMO tipo que el @TenantId de Asunto

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoDoc estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "url_archivo_snapshot")
    private String urlArchivoSnapshot; // Aquí guardaremos el link al PDF estático

    @CreationTimestamp
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;
}
