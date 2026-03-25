package com.lexflow.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexflow.api.model.HistorialEstadoDocumento;

@Repository
public interface HistorialEstadoDocumentoRepository extends JpaRepository <HistorialEstadoDocumento,Long> {
    
}
