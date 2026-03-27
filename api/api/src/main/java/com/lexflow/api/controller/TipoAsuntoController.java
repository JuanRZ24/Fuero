package com.lexflow.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lexflow.api.service.TipoAsuntoService;

import com.lexflow.api.model.TipoAsunto;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-asunto")
public class TipoAsuntoController {

    private final TipoAsuntoService tipoAsuntoService;

    public TipoAsuntoController(TipoAsuntoService tipoAsuntoService) {
        this.tipoAsuntoService = tipoAsuntoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoAsunto>> getAllTipoAsunto() {
        List<TipoAsunto> tipoAsuntoList = tipoAsuntoService.getAllTipoAsunto();
        return ResponseEntity.ok(tipoAsuntoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoAsunto> getTipoAsuntoById(@PathVariable Long id) {
        TipoAsunto tipoAsunto = tipoAsuntoService.getTipoAsuntoById(id);
        return ResponseEntity.ok(tipoAsunto);
    }

    @PostMapping
    public ResponseEntity<TipoAsunto> createTipoAsunto(@RequestBody TipoAsunto tipoAsunto) {
        TipoAsunto createdTipoAsunto = tipoAsuntoService.createTipoAsunto(tipoAsunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTipoAsunto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoAsunto> updateTipoAsunto(@PathVariable Long id, @RequestBody TipoAsunto tipoAsuntoDetails) {
        TipoAsunto updatedTipoAsunto = tipoAsuntoService.updateTipoAsunto(id, tipoAsuntoDetails);
        return ResponseEntity.ok(updatedTipoAsunto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTipoAsunto(@PathVariable Long id) {
        tipoAsuntoService.deleteTipoAsunto(id);
        return ResponseEntity.noContent().build();
    }


}

