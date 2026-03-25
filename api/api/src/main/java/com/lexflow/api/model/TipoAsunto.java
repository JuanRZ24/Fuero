package com.lexflow.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_asunto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAsunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}