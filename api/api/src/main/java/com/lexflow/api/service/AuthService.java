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
@RequiredArgsConstructor // 🔥 Esto crea el constructor automáticamente para los "private final"
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final DespachoRepository despachoRepository; // ✅ Agregado para poder guardar despachos
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse registrarNuevoDespacho(RegistroDespachoRequest request) {
        
        // 1. Creamos el Despacho (Tabla Global)
        Despacho despacho = new Despacho();
        despacho.setNombre(request.getNombreDespacho());
        
        // 🔥 TODO: Aquí meteremos la llamada a Google Drive en el próximo paso
        // String folderId = googleDriveService.crearCarpetaDespacho(despacho.getNombre());
        // despacho.setCarpetaDriveId(folderId);

        Despacho despachoGuardado = despachoRepository.save(despacho);

        // 2. Creamos al Usuario Administrador (Dueño del despacho)
        Usuario admin = new Usuario();
        admin.setNombre(request.getNombreAbogado());
        admin.setEmail(request.getEmail());
        
        // Súper importante: Hashear la contraseña antes de guardarla
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRol(RolUsuario.ADMINISTRADOR); 
        admin.setDespacho(despachoGuardado); // Lo amarramos a su burbuja
        
        usuarioRepository.save(admin);

        // 3. Le damos su llave de acceso (JWT con su despacho_id inyectado)
        String token = jwtService.generarToken(admin);
        
        return AuthResponse.builder()
        .token(token)
        .mensaje("Registro exitoso")
        .nombre(admin.getNombre())
        .rol(admin.getRol().name())
        // Nota: Si en el registro también quieres darle un Refresh Token, 
        // puedes llamar a tu método crearRefreshTokenParaUsuario(admin) y agregarlo aquí.
        .build();
    }

@Transactional    
public AuthResponse login(LoginRequest request) {
        // 🔥 LA SOLUCIÓN: Usamos el método nativo que esquiva el TenantId
        Usuario usuario = usuarioRepository.findByEmailParaLogin(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Credenciales incorrectas."));

        // 2. Comparamos la contraseña de texto plano con el Hash de la base de datos
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
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