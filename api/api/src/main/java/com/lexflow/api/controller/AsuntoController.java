package com.lexflow.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lexflow.api.dto.AsuntoDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.service.AsuntoService;



@RestController
@RequestMapping("/api/asuntos")
public class AsuntoController {

    private final AsuntoService  asuntoService;

    public AsuntoController(AsuntoService  asuntoService) {
        this.asuntoService = asuntoService;
    }

    @GetMapping
    public ResponseEntity<List<AsuntoDTO>> getAllAsuntos(){
        List<AsuntoDTO> asuntos = asuntoService.getAll();
        return ResponseEntity.ok(asuntos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsuntoDTO> getAsuntoById(@PathVariable Long id) {
        AsuntoDTO asunto = asuntoService.getById(id);
        return ResponseEntity.ok(asunto);
    }


    @PostMapping
    public ResponseEntity<AsuntoDTO> createAsunto(@RequestBody AsuntoDTO dto) {
        AsuntoDTO saved = asuntoService.save(dto);
        return ResponseEntity.ok(saved);
    }


    @PutMapping("/{id}")
    public ResponseEntity<AsuntoDTO> updateAsunto(@PathVariable Long id, @RequestBody AsuntoDTO dto) {
        return ResponseEntity.ok(asuntoService.update(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsunto(@PathVariable Long id) {
        if (asuntoService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build(); 
    }

    @PostMapping("/{asuntoId}/participantes/{usuarioId}")
    public ResponseEntity<Void> addParticipant(@PathVariable Long asuntoId, @PathVariable Long usuarioId) {
        asuntoService.addParticipant(asuntoId, usuarioId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{asuntoId}/participantes")
    public ResponseEntity<List<Usuario>> getLegalTeam(@PathVariable Long asuntoId) {
        return ResponseEntity.ok(asuntoService.getLegalTeam(asuntoId));
    }


    
}