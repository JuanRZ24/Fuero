package com.lexflow.api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lexflow.api.dto.TareaRequestDTO;
import com.lexflow.api.model.Tarea;
import com.lexflow.api.service.TareaService;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {
    
    private final TareaService tareaService;
    

    public TareaController(TareaService tareaService){
        this.tareaService = tareaService;
    }


    @GetMapping
    public ResponseEntity<List<Tarea>> ObtenerTareas(){
        return ResponseEntity.ok(tareaService.obtenerTareas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> ObtenerTarea(@PathVariable Long Id){
        return tareaService.obtenerTarea(Id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Tarea> crearTarea(@RequestBody TareaRequestDTO request) {
        // Le pasamos solo la petición, el servicio se encarga de averiguar quién es el jefe
        Tarea tareaGuardada = tareaService.crearTarea(request);
        return new ResponseEntity<>(tareaGuardada, HttpStatus.CREATED);
    }




}
