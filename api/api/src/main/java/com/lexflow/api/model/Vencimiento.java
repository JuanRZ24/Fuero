package com.lexflow.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vencimientos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vencimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaLimite;

    @Column(nullable = false)
    private String descripcion;

    // Para saber si el abogado ya cumplió con este término
    @Builder.Default
    private boolean completado = false; 

    // Relación con el expediente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asunto_id", nullable = false)
    @JsonIgnore // Evitamos bucles infinitos al serializar
    private Asunto asunto;

    // Relación OPCIONAL con la etapa (puede ser un término general del caso)
    @ManyToOne
    @JoinColumn(name = "etapa_id")
    private EtapaProcesal etapa;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario creadoPor;
}