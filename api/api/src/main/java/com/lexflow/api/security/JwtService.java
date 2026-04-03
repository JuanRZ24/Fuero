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
        // 1. Iniciamos la construcción del token
        var jwtBuilder = JWT.create()
                .withSubject(usuario.getEmail()) 
                .withClaim("rol", usuario.getRol().name()) 
                .withIssuedAt(new Date()) 
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
        
        // 2. Inyectamos el despachoId de forma dinámica
        if (usuario.getDespacho() != null) {
            jwtBuilder.withClaim("despachoId", usuario.getDespacho().getId());
        }

        // 3. Firmamos y cerramos el token
        return jwtBuilder.sign(algorithm); 
    }

    // Método para leer el token y sacar el correo
    public String extraerEmail(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject(); 
        } catch (Exception e) {
            return null; 
        }
    }

    
    public Long extraerDespachoId(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            
            // Extraemos el claim. Si no existe o es nulo, asLong() devolverá null.
            return decodedJWT.getClaim("despachoId").asLong();
        } catch (Exception e) {
            return null;
        }
    }
}