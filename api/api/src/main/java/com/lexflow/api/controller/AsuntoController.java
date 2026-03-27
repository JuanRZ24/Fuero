package com.lexflow.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lexflow.api.model.Asunto;
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
    public ResponseEntity<Asunto> obtenerCliente(@PathVariable Long id){
        return asuntoService.obtenerAsunto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Asunto> guardarAsunto(@RequestBody Asunto nuevoAsunto){
        asuntoService.guardarAsunto(nuevoAsunto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAsunto);
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



    
}