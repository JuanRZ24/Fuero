package com.lexflow.api.controller;

import com.lexflow.api.dto.MovimientoProcesalDTO;
import com.lexflow.api.service.MovimientoProcesalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoProcesalController {

    private final MovimientoProcesalService movimientoService;

    @GetMapping("/asunto/{asuntoId}")
    public ResponseEntity<List<MovimientoProcesalDTO>> getHistory(@PathVariable Long asuntoId) {
        return ResponseEntity.ok(movimientoService.getHistory(asuntoId));
    }

    @PostMapping
    public ResponseEntity<Void> createMovement(
            @RequestBody MovimientoProcesalDTO request,
            Principal principal 
    ) {
        movimientoService.createMovement(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}