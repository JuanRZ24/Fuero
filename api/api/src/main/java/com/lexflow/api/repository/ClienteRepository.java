package com.lexflow.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexflow.api.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository <Cliente,Long> {
    
}
