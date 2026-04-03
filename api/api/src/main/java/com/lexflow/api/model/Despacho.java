package com.lexflow.api.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "despachos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String rfc;

    @Column(name = "carpeta_drive_id")
    private String carpetaDriveId;

    // --- Campos Comerciales / SaaS ---
    @Column(name = "limite_usuarios_gratis")
    @Builder.Default
    private Integer limiteUsuariosGratis = 5;

    @Column(name = "precio_usuario_extra", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioUsuarioExtra = new BigDecimal("5.00");

    @Column(name = "tipo_plan")
    @Builder.Default
    private String tipoPlan = "BASIC";

    @Column(name = "fecha_vencimiento_plan")
    private LocalDateTime fechaVencimientoPlan;

    // --- Control Interno ---
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}