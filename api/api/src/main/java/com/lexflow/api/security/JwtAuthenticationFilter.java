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
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Buscamos la cabecera Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay token o no tiene el formato "Bearer ", lo ignoramos
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extraemos el JWT
            final String token = authHeader.substring(7);
            final String userEmail = jwtService.extraerEmail(token);

            // 4. Si el token es válido y el usuario aún no está logueado en este request
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 🔥 Extraemos el despachoId del token y lo guardamos en la "cajita fuerte"
                Long despachoId = jwtService.extraerDespachoId(token);
                if (despachoId != null) {
                    TenantContext.setCurrentTenant(despachoId);
                } else {
                    // Si el token es viejo y no trae despacho, lo mandamos al inquilino 0 por seguridad
                    TenantContext.setCurrentTenant(0L);
                }
            }
        } catch (Exception e) {
            // 🔥 EL ESCUDO: Atrapamos cualquier explosión interna (ClassCastException, nulos, base de datos)
            // Esto evitará que el backend falle en silencio y nos dirá la raíz del 403.
            System.err.println("❌ ERROR FATAL EN EL FILTRO JWT: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            
            // Limpiamos el contexto por seguridad si descubrimos un token corrupto
            SecurityContextHolder.clearContext();
        }
        
        try {
            // 6. Dejamos que la petición continúe hacia tu Controlador
            filterChain.doFilter(request, response);
        } finally {
            // Siempre, pase lo que pase, vaciamos la cajita fuerte al terminar la petición
            TenantContext.clear();
        }
    }
}