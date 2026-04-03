package com.lexflow.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lexflow.api.model.Despacho;
import com.lexflow.api.service.DespachoService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.Data;

@RestController
@RequestMapping("/api/despachos")
@Data
public class DespachoController {
    

    private final DespachoService despachoService;


    @GetMapping
    public ResponseEntity<List<Despacho>> listarDespachos(){
        List<Despacho> despachos = despachoService.obtenerTodos();
        return ResponseEntity.ok(despachos);
    }


    @GetMapping
    public ResponseEntity<Despacho> obtenerDespacho(@PathVariable Long id){
        return despachoService.obtenerDespacho(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Despacho> guardarDespacho(@RequestBody Despacho nuevoDespacho){
        despachoService.guardarDespacho(nuevoDespacho);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDespacho);
    }
}
