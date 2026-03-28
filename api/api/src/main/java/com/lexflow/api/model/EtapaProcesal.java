package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etapas_procesales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaProcesal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Contestación de Demanda"

    @Column(nullable = false)
    private Integer orden; // Ej: 1, 2, 3... (Para saber qué sigue después)

    @Column(name = "es_etapa_final")
    private Boolean esEtapaFinal = false; // Para saber si al llegar aquí se cierra el caso

    // --- RELACIONES ---
    
    // A qué "Tipo de Juicio" pertenece esta etapa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_asunto_id", nullable = false)
    private TipoAsunto tipoAsunto; 
}