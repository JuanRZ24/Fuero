package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE tareas SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el Asunto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asunto_id", nullable = false)
    private Asunto asunto;

    // Relación con el Usuario (a quién se le asigna)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado_id", nullable = false)
    private Usuario usuarioAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id", nullable = false)
    private Usuario usuarioCreador;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}