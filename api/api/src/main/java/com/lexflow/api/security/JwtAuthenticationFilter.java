package com.lexflow.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Interfaz nativa de Spring para buscar usuarios

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Buscamos la cabecera Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay token o no tiene el formato "Bearer ", lo ignoramos (luego Spring lo bloqueará)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el JWT (quitando los primeros 7 caracteres: "Bearer ")
        final String token = authHeader.substring(7);
        final String userEmail = jwtService.extraerEmail(token); // Usamos el método que acabamos de crear

        // 4. Si el token es válido (nos devolvió el email) y el usuario aún no está logueado en este request
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Buscamos al usuario en la base de datos usando Spring Security
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. ¡LA LÍNEA MÁGICA! Le decimos a Spring Boot: "Este usuario entró con token legítimo"
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Lo guardamos en el contexto de seguridad para que tu TareaService pueda leerlo sin error 500
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        
        // 6. Dejamos que la petición continúe hacia tu Controlador
        filterChain.doFilter(request, response);
    }
}