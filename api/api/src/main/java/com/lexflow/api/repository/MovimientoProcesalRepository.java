package com.lexflow.api.repository;

import com.lexflow.api.model.MovimientoProcesal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoProcesalRepository extends JpaRepository<MovimientoProcesal, Long> {
    // Magia de Spring Data: Busca por ID del asunto y ordena por fecha descendente
    List<MovimientoProcesal> findByAsuntoIdOrderByFechaMovimientoDesc(Long asuntoId);
}