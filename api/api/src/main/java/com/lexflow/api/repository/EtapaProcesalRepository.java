package com.lexflow.api.repository;

import com.lexflow.api.model.EtapaProcesal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtapaProcesalRepository extends JpaRepository<EtapaProcesal, Long> {
    
    // 🔥 LA MAGIA PARA TU FRONTEND 🔥
    // Busca todas las etapas de un tipo de asunto (Ej: Amparo) y las ordena 1, 2, 3...
    List<EtapaProcesal> findByTipoAsuntoIdOrderByOrdenAsc(Long tipoAsuntoId);
    
}