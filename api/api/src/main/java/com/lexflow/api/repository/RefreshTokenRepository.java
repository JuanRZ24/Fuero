package com.lexflow.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lexflow.api.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository <RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUsuarioId(Long usuarioId);
    
}
