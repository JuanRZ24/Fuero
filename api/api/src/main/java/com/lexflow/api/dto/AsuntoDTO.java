package com.lexflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import org.hibernate.annotations.TenantId;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsuntoDTO {
    
    private Long id; // Lo usamos cuando devolvemos la info al frontend
    
    private String titulo;
    
    private String descripcion;
    
    // IDs planos para las relaciones (¡Mucho más fácil para React!)
    private Long clienteId;
    private Long tipoAsuntoId;

    // 🔥 La magia para que PostgreSQL lo entienda como JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "campos_dinamicos", columnDefinition = "jsonb")
    private Map<String, Object> camposDinamicos;


}