package com.lexflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;         // El Access Token (Gafete VIP de 15 mins)
    private String refreshToken;  // La Llave Maestra (Dura días)
    private String mensaje;
    private String nombre;
    private String rol;
}