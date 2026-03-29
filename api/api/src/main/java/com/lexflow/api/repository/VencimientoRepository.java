package com.lexflow.api.repository;

import com.lexflow.api.model.Vencimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VencimientoRepository extends JpaRepository<Vencimiento, Long> {

    // Para ver todos los vencimientos de un expediente en específico
    List<Vencimiento> findByAsuntoIdOrderByFechaLimiteAsc(Long asuntoId);

    // Para el Dashboard: Traer los pendientes más urgentes de todo el despacho
    @Query("SELECT v FROM Vencimiento v WHERE v.completado = false ORDER BY v.fechaLimite ASC")
    List<Vencimiento> findPendientesProximos();
}