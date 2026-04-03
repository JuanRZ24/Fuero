package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.TenantId;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "movimientos_procesales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoProcesal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "despacho_id")
    private Long despachoId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id", insertable = false, updatable = false)
    private Despacho despacho;

    @Column(nullable = false)
    private String titulo; // Ej: "Notificación de Sentencia"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion; // Ej: "El actuario dejó citatorio en el domicilio..."

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento; 

    // 🔥 PREPARANDO EL TERRENO PARA LA FASE C (Vencimientos) 🔥
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // Ej: Fecha límite para contestar (opcional)

    // --- RELACIONES ---

    // A qué caso pertenece este movimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asunto_id", nullable = false)
    private Asunto asunto;

    // Qué abogado del despacho registró este movimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    // (Opcional) Si este movimiento nació porque subieron un PDF
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    // "El documento se liga, no modifica"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_vinculada_id")
    private EtapaProcesal etapaVinculada;

    @PrePersist
    protected void onCreate() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }
}