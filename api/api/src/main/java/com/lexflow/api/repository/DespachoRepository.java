package com.lexflow.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lexflow.api.model.Despacho;

public interface DespachoRepository extends JpaRepository <Despacho,Long> {
    
}
