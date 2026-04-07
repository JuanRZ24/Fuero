package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.TenantId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE documentos SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "despacho_id")
    private Long despachoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "despacho_id", insertable = false, updatable = false)
    private Despacho despacho;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // --- METADATOS TÉCNICOS ---
    
    @Column(name = "tipo_mime")
    private String tipoMime; // Ej: "application/pdf"

    @Column(name = "tamano")
    private Long tamano; // Tamaño en bytes

   @Column(name = "ruta_archivo")
private String rutaArchivo;

    // --- RELACIONES ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asunto_id", nullable = false)
    private Asunto asunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    // --- AUDITORÍA Y ESTADOS ---

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision")
    private EstadoDoc estadoRevision;

    // "El documento se liga, no modifica"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_vinculada_id")
    private EtapaProcesal etapaVinculada;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Magic de JPA para poner la fecha automáticamente al crear
    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
        if (estadoRevision == null) {
            estadoRevision = EstadoDoc.PENDIENTE; // Estado por defecto
        }
    }
}