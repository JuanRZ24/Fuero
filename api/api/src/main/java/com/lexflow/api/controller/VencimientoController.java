package com.lexflow.api.controller;

import com.lexflow.api.dto.VencimientoDTO;
import com.lexflow.api.service.VencimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vencimientos")
@CrossOrigin(origins = "*") // ¡NUNCA OLVIDAR ESTO PARA REACT!
@RequiredArgsConstructor
public class VencimientoController {

    private final VencimientoService vencimientoService;

    // 1. Crear un nuevo vencimiento (Lo usaremos desde el Case Detail)
    @PostMapping
    public ResponseEntity<VencimientoDTO.Response> crearVencimiento(@RequestBody VencimientoDTO.Request request) {
        return ResponseEntity.ok(vencimientoService.crear(request));
    }

    // 2. Traer los próximos a vencer para el Dashboard
    @GetMapping("/pendientes")
    public ResponseEntity<List<VencimientoDTO.Response>> obtenerPendientes() {
        return ResponseEntity.ok(vencimientoService.obtenerPendientesDashboard());
    }

    // 3. Traer todos los vencimientos de un caso específico
    @GetMapping("/asunto/{asuntoId}")
    public ResponseEntity<List<VencimientoDTO.Response>> obtenerPorAsunto(@PathVariable Long asuntoId) {
        return ResponseEntity.ok(vencimientoService.obtenerPorAsunto(asuntoId));
    }

    // 4. Marcar como completado (Checkbox en React)
    @PutMapping("/{id}/completar")
    public ResponseEntity<Void> completarVencimiento(@PathVariable Long id) {
        vencimientoService.marcarComoCompletado(id);
        return ResponseEntity.ok().build();
    }
}