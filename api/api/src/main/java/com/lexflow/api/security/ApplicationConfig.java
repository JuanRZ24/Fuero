package com.lexflow.api.security;

import com.lexflow.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UsuarioRepository usuarioRepository;

    // 1. Le decimos a Spring cómo buscar a un usuario por su email
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByEmailForLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // 2. El proveedor de autenticación (el que junta el UserDetailsService con el encriptador de contraseñas)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // ¡NUEVO ESTÁNDAR! Le pasamos el servicio directamente en el constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        
        // Solo le seteamos el encriptador de contraseñas
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    // 3. El mánager que usaremos en tu AuthController para hacer el Login real
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. El encriptador oficial de Spring (BCrypt) para que nadie vea las contraseñas en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}