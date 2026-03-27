package com.lexflow.api.service;

import com.lexflow.api.dto.AuthResponse;
import com.lexflow.api.dto.LoginRequest;

import com.lexflow.api.model.RefreshToken;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.RefreshTokenRepository;
import com.lexflow.api.repository.UsuarioRepository;
import com.lexflow.api.security.JwtService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }



    public AuthResponse login(LoginRequest request) {
        // 1. Buscamos al usuario por correo
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Credenciales incorrectas.")); // Mensaje genérico por seguridad

        // 2. Comparamos la contraseña de texto plano con el Hash de la base de datos
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Error: Credenciales incorrectas.");
        }

        // 3. Si todo coincide, le damos su Token
        String token = jwtService.generarToken(usuario);

        RefreshToken refreshToken = crearRefreshTokenParaUsuario(usuario);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .mensaje("Login exitoso")
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    private RefreshToken crearRefreshTokenParaUsuario(Usuario usuario) {
        
        
        RefreshToken refreshToken = refreshTokenRepository.findByUsuarioId(usuario.getId())
                .orElse(new RefreshToken()); 

       
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString()); 
        refreshToken.setFechaExpiracion(Instant.now().plus(7, ChronoUnit.DAYS)); 
        refreshToken.setRevocado(false); 
                
       
        return refreshTokenRepository.save(refreshToken);
    }

}