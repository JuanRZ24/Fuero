package com.lexflow.api.model;

import java.util.UUID;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asuntos_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsuntoUsuario {

    @EmbeddedId
    @Builder.Default
    private AsuntoUsuarioId id = new AsuntoUsuarioId();

    // @MapsId le dice a Hibernate: "Saca el ID de este objeto y mételo en la llave compuesta"
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("asuntoId") 
    @JoinColumn(name = "asunto_id")
    private Asunto asunto;

    @TenantId
    @Column(name = "despacho_id", nullable = false, updatable = false)
    private Long despachoId;   // usa el MISMO tipo que el @TenantId de Asunto

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "es_responsable", nullable = false)
    @Builder.Default
    private Boolean esResponsable = false;
}