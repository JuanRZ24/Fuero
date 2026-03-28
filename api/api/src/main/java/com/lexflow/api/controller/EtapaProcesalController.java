package com.lexflow.api.controller;

import com.lexflow.api.dto.EtapaProcesalDTO;
import com.lexflow.api.service.EtapaProcesalService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etapas")
@RequiredArgsConstructor
public class EtapaProcesalController {

    private final EtapaProcesalService etapaService;

    // React llamará a este endpoint pasándole el ID del Tipo de Juicio
    // Ej: GET /api/etapas/tipo/1
    @GetMapping("/tipo/{tipoAsuntoId}")
    public ResponseEntity<List<EtapaProcesalDTO>> obtenerEtapasPorTipo(@PathVariable Long tipoAsuntoId) {
        return ResponseEntity.ok(etapaService.obtenerEtapasPorTipoAsunto(tipoAsuntoId));
    }


    // Dentro de EtapaProcesalController.java

@PostMapping
public ResponseEntity<EtapaProcesalDTO> crearEtapa(@RequestBody EtapaProcesalDTO dto) {
    EtapaProcesalDTO guardada = etapaService.guardarEtapa(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
}
}