package com.lexflow.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsuntoUsuarioId implements Serializable {
    
    // JPA requiere que las llaves compuestas implementen Serializable
    
    @Column(name = "asunto_id")
    private Long asuntoId;

    @Column(name = "usuario_id")
    private Long usuarioId;
}