package com.lexflow.api.repository;

import com.lexflow.api.model.AsuntoUsuario;
import com.lexflow.api.model.AsuntoUsuarioId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsuntoUsuarioRepository extends JpaRepository<AsuntoUsuario, AsuntoUsuarioId> {
        boolean existsById_AsuntoIdAndId_UsuarioId(Long asuntoId, Long usuarioId);
        List<AsuntoUsuario> findById_AsuntoId(Long asuntoId);
}