package com.lexflow.api.controller;

import com.lexflow.api.model.EtapaProcesal;
import com.lexflow.api.service.EtapaProcesalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etapas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EtapaProcesalController {

    // 🔥 Ahora inyectamos el SERVICE, no el Repository
    private final EtapaProcesalService etapaService;

    @GetMapping
    public ResponseEntity<List<EtapaProcesal>> obtenerTodas() {
        return ResponseEntity.ok(etapaService.obtenerTodas());
    }

    @GetMapping("/tipo-asunto/{tipoId}")
    public ResponseEntity<List<EtapaProcesal>> obtenerPorTipo(@PathVariable Long tipoId) {
        return ResponseEntity.ok(etapaService.obtenerPorTipo(tipoId));
    }
}