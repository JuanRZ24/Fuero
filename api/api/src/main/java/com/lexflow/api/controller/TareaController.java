package com.lexflow.api.controller;

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
    public ResponseEntity<List<Tarea>> getAllTareas(){
        return ResponseEntity.ok(tareaService.getTareas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> getTareaById(@PathVariable Long id){
        return tareaService.getTarea(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Tarea> createTarea(@RequestBody TareaRequestDTO request) {
        Tarea tareaGuardada = tareaService.createTarea(request);
        return new ResponseEntity<>(tareaGuardada, HttpStatus.CREATED);
    }




}