package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.TenantId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asuntos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE asuntos SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Asunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @TenantId
    @Column(name = "despacho_id")
    private Long despachoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id", insertable = false, updatable = false)
    @JsonIgnore
    private Despacho despacho;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_asunto_id", nullable = false)
    private TipoAsunto tipoAsunto;

    @Column(name = "acto_impugnar", columnDefinition = "TEXT")
    private String actoImpugnar;

    @Column(name = "fecha_acto")
    private LocalDate fechaActo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_actual_id")
    private EtapaProcesal etapaActual;

    @Column(name = "fecha_limite_legal")
    private LocalDate fechaLimiteLegal;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "estado", length = 50)
    private String estado = "ACTIVO";



}