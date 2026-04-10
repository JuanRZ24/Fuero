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
    public ResponseEntity<List<Asunto>> listarAsuntos(){
        List<Asunto> asuntos = asuntoService.obtenerTodos();
        return ResponseEntity.ok(asuntos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsuntoDTO> obtenerAsuntoPorId(@PathVariable Long id) {
        AsuntoDTO asunto = asuntoService.obtenerAsuntoPorId(id);
        return ResponseEntity.ok(asunto);
    }


    @PostMapping
    public ResponseEntity<AsuntoDTO> crearAsunto(@RequestBody AsuntoDTO dto) {
        AsuntoDTO guardado = asuntoService.guardarAsunto(dto);
        return ResponseEntity.ok(guardado);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Asunto> actualizarCliente(@PathVariable Long id, @RequestBody Asunto asuntoActualizado) {
        return asuntoService.actualizar(id, asuntoActualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        if (asuntoService.eliminar(id)) {
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content si fue exitoso
        }
        return ResponseEntity.notFound().build(); 
    }

    @PostMapping("/{asuntoId}/participantes/{usuarioId}")
public ResponseEntity<Void> agregarParticipante(@PathVariable Long asuntoId, @PathVariable Long usuarioId) {
    asuntoService.agregarParticipante(asuntoId, usuarioId);
    return ResponseEntity.ok().build();
}

@GetMapping("/{asuntoId}/participantes")
    public ResponseEntity<List<Usuario>> obtenerEquipoLegal(@PathVariable Long asuntoId) {
        return ResponseEntity.ok(asuntoService.obtenerEquipoLegal(asuntoId));
    }


    
}