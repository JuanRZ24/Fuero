package com.lexflow.api.controller;

import com.lexflow.api.model.Plantilla;
import com.lexflow.api.dto.PlantillaDTO;
import com.lexflow.api.service.PlantillaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @GetMapping
public ResponseEntity<List<PlantillaDTO>> obtenerPlantillas() {
    // Usamos el DTO para evitar bucles infinitos y errores de Lazy Loading
    List<PlantillaDTO> plantillas = plantillaService.obtenerPlantillasPorDespacho();
    return ResponseEntity.ok(plantillas);
}
}