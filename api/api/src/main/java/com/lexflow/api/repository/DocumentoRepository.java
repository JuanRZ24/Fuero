package com.lexflow.api.repository;

import com.lexflow.api.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoRepository extends JpaRepository <Documento, Long> {
    
}
