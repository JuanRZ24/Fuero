package com.lexflow.api.dto;

import lombok.Data;

@Data
public class RegistroDespachoRequest {
    // Datos del negocio
    private String nombreDespacho;
    // Opcional: private String telefonoDespacho;

    // Datos del abogado administrador
    private String nombreAbogado;
    private String email;
    private String password;
}