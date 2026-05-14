package com.lexflow.api.controller;

import com.lexflow.api.dto.VencimientoDTO;
import com.lexflow.api.service.VencimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vencimientos")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class VencimientoController {

    private final VencimientoService vencimientoService;

    @PostMapping
    public ResponseEntity<VencimientoDTO.Response> createVencimiento(@RequestBody VencimientoDTO.Request request) {
        return ResponseEntity.ok(vencimientoService.create(request));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<VencimientoDTO.Response>> getPendingVencimientos() {
        return ResponseEntity.ok(vencimientoService.getPendingDashboard());
    }

    @GetMapping("/asunto/{asuntoId}")
    public ResponseEntity<List<VencimientoDTO.Response>> getVencimientosByAsunto(@PathVariable Long asuntoId) {
        return ResponseEntity.ok(vencimientoService.getByAsunto(asuntoId));
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> completeVencimiento(@PathVariable Long id) {
        vencimientoService.markAsCompleted(id);
        return ResponseEntity.ok().build();
    }
}