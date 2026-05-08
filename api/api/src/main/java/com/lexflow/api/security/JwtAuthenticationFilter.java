package com.lexflow.api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Slf4j // Agregamos Slf4j para logs profesionales en lugar de System.err
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

        // 2. Si NO hay token (puede ser un endpoint público como /login), dejamos que el filtro siga su curso normal
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
                    TenantContext.setCurrentTenant(0L);
                }
            }
            
            // 5. 🔥 EXITO: Solo si TODO salió perfecto y no hubo excepciones, dejamos pasar la petición al Controlador
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("🚨 Token expirado: {}", e.getMessage());
            manejarErrorDeSeguridad(response, "El token de acceso ha expirado.", HttpStatus.UNAUTHORIZED);
            
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("🚨 Token corrupto o firma inválida: {}", e.getMessage());
            manejarErrorDeSeguridad(response, "El token de acceso es inválido.", HttpStatus.UNAUTHORIZED);
            
        } catch (Exception e) {
            // 6. 🔥 EL ESCUDO ACTIVO: Si el token está roto o falta un dato, atrapamos el error aquí.
            // COMO NO LLAMAMOS A filterChain.doFilter() AQUÍ, LA PETICIÓN MUERE Y NUNCA LLEGA AL CONTROLADOR.
            log.error("💥 Error fatal procesando el JWT", e);
            manejarErrorDeSeguridad(response, "Error interno de autenticación.", HttpStatus.INTERNAL_SERVER_ERROR);
            
        } finally {
            // 7. Pase lo que pase (éxito o error), siempre limpiamos la cajita fuerte de multi-tenant
            TenantContext.clear();
        }
    }

    /**
     * Helper para construir una respuesta JSON bonita para React cuando falla la seguridad.
     */
    private void manejarErrorDeSeguridad(HttpServletResponse response, String mensaje, HttpStatus status) throws IOException {
        SecurityContextHolder.clearContext(); // Limpiamos rastros por seguridad
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Armamos un JSON a mano para no depender de ObjectMapper aquí
        String jsonError = String.format("{\"error\": \"%s\", \"status\": %d}", mensaje, status.value());
        response.getWriter().write(jsonError);
        response.getWriter().flush();
    }
}