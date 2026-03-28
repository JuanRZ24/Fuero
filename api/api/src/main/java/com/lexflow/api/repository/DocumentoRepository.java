package com.lexflow.api.repository;

import com.lexflow.api.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    // Para listar todos los documentos de un caso legal
    List<Documento> findByAsuntoId(Long asuntoId);
}