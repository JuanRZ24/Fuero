package com.lexflow.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lexflow.api.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    // En un proyecto real y en producción, esta llave se esconde en las variables de entorno.
    // Por ahora la dejamos aquí. ¡Debe ser una frase larga para que el algoritmo sea seguro!
    private static final String SECRET_KEY = "LexFlowSecretKeySuperSeguraYSuperLargaParaQueNoFalle";

    public String generarToken(Usuario usuario) {
        // Usamos el algoritmo HMAC256 para encriptar
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        
        return JWT.create()
                .withSubject(usuario.getEmail()) // El "dueño" del token es el email
                .withClaim("rol", usuario.getRol().name()) // Guardamos el rol (Admin, Proyectista, etc.)
                .withIssuedAt(new Date()) // Fecha de creación
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Caduca en exactamente 24 horas
                .sign(algorithm); // Lo firmamos con nuestra llave secreta
    }
}