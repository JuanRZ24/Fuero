package com.lexflow.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexflow.api.model.Asunto;

@Repository
public interface AsuntoRepository extends JpaRepository <Asunto,Long> {
    
}
