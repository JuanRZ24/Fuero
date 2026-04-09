package com.lexflow.api.controller;

import com.lexflow.api.dto.PlantillaDTO;
import com.lexflow.api.service.PlantillaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plantillas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // Obligamos a usar token
public class PlantillaController {

    private final PlantillaService plantillaService;

    @PostMapping
    public ResponseEntity<PlantillaDTO> crearPlantilla(@RequestBody PlantillaDTO dto) {
        PlantillaDTO guardada = plantillaService.crearPlantilla(dto);
        return ResponseEntity.ok(guardada);
    }
}