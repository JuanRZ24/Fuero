package com.lexflow.api.repository;

import com.lexflow.api.model.AsuntoUsuario;
import com.lexflow.api.model.AsuntoUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsuntoUsuarioRepository extends JpaRepository<AsuntoUsuario, AsuntoUsuarioId> {
    
}