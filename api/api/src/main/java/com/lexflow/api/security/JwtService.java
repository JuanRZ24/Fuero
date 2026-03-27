package com.lexflow.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lexflow.api.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "LexFlowSecretKeySuperSeguraYSuperLargaParaQueNoFalle";
    
    // Instanciamos el algoritmo una sola vez para reusarlo
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
    
    public String generarToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail()) 
                .withClaim("rol", usuario.getRol().name()) 
                .withIssuedAt(new Date()) 
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) 
                .sign(algorithm); 
    }

    // ¡NUEVO! Método para leer el token y sacar el correo
    public String extraerEmail(String token) {
        try {
            // El Verifier checa automáticamente la firma y la fecha de expiración
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            
            // Si llega hasta aquí, el token es 100% legítimo
            return decodedJWT.getSubject(); 
        } catch (Exception e) {
            // Si el token expiró o es falso, devolvemos null para que Spring Security bloquee el acceso
            return null; 
        }
    }
}