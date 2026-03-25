package com.lexflow.api.repository;

import com.lexflow.api.model.TipoAsunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoAsuntoRepository extends JpaRepository<TipoAsunto, Long> {
    
}
