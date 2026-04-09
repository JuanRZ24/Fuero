package com.lexflow.api.model;

import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampoPlantilla {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreLabel; // Ej: "¿Tiene hijos?"
    private String nombreKey;   // Ej: "tiene_hijos" (Esta será la llave en el JSONB)
    
    @Enumerated(EnumType.STRING)
    private TipoCampo tipo; // TEXTO, NUMERO, CHECKBOX, FECHA

    private boolean requerido;

    @ManyToOne
    @JoinColumn(name = "plantilla_id")
    private Plantilla plantilla;
}
