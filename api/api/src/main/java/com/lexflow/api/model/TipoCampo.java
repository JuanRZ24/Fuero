package com.lexflow.api.model; // O el paquete donde lo vayas a guardar

public enum TipoCampo {
    TEXTO,      // Para inputs normales (ej. Juzgado, Nombre)
    NUMERO,     // Para inputs de dinero o cantidades (ej. Monto Demandado)
    FECHA,      // Para calendarios (ej. Fecha de Audiencia)
    BOOLEANO,   // Para checkboxes o switches (ej. ¿Requiere Perito? Sí/No)
    SELECCION   // Para listas desplegables (ej. Tipo de Contrato: Fijo, Temporal)
}