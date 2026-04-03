package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id")
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

    // --- INTEGRACIÓN CON GOOGLE DRIVE ---

    @Column(name = "google_doc_id", unique = true)
    private String googleDocId; // El ID que usaremos para el visualizador en React

    @Column(name = "google_doc_url", length = 500)
    private String googleDocUrl; // El link directo por si quieren abrirlo en otra pestaña

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