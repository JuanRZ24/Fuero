package com.lexflow.api.service;

import com.lexflow.api.dto.AuthResponse;
import com.lexflow.api.dto.LoginRequest;
import com.lexflow.api.dto.RegistroDespachoRequest;
import com.lexflow.api.model.Despacho;
import com.lexflow.api.model.RefreshToken;
import com.lexflow.api.model.RolUsuario;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.DespachoRepository;
import com.lexflow.api.repository.RefreshTokenRepository;
import com.lexflow.api.repository.UsuarioRepository;
import com.lexflow.api.security.JwtService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final DespachoRepository despachoRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse registerNewDespacho(RegistroDespachoRequest request) {
        
        Despacho despacho = new Despacho();
        despacho.setNombre(request.getNombreDespacho());

        Despacho savedDespacho = despachoRepository.save(despacho);

        Usuario admin = new Usuario();
        admin.setNombre(request.getNombreAbogado());
        admin.setEmail(request.getEmail());
        
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRol(RolUsuario.ADMINISTRADOR); 
        admin.setDespacho(savedDespacho); 
        
        usuarioRepository.save(admin);

        String token = jwtService.generarToken(admin);
        
        return AuthResponse.builder()
        .token(token)
        .mensaje("Registro exitoso")
        .nombre(admin.getNombre())
        .rol(admin.getRol().name())
        .build();
    }

    @Transactional    
    public AuthResponse login(LoginRequest request) {
        Usuario user = usuarioRepository.findByEmailForLogin(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Credenciales incorrectas."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Error: Credenciales incorrectas.");
        }

        String token = jwtService.generarToken(user);

        RefreshToken refreshToken = createRefreshTokenForUsuario(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .mensaje("Login exitoso")
                .nombre(user.getNombre())
                .rol(user.getRol().name())
                .build();
    }

    private RefreshToken createRefreshTokenForUsuario(Usuario user) {
        
        RefreshToken refreshToken = refreshTokenRepository.findByUsuarioId(user.getId())
                .orElse(new RefreshToken()); 

        refreshToken.setUsuario(user);
        refreshToken.setToken(UUID.randomUUID().toString()); 
        refreshToken.setFechaExpiracion(Instant.now().plus(7, ChronoUnit.DAYS)); 
        refreshToken.setRevocado(false); 
                
        return refreshTokenRepository.save(refreshToken);
    }
}