package com.lexflow.api.controller;

import com.lexflow.api.dto.PlantillaDTO;
import com.lexflow.api.service.PlantillaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // Obligamos a usar token
public class PlantillaController {

    private final PlantillaService plantillaService;

    @PreAuthorize("hasAuthority('ROLE_COORDINADOR')") 
    @PostMapping
    public ResponseEntity<PlantillaDTO> crearPlantilla(@RequestBody PlantillaDTO dto) {
        return ResponseEntity.ok(plantillaService.crearPlantilla(dto));
    }

    // 📖 Todos en el despacho pueden ver las plantillas (Coordinadores y Proyectistas)
    @PreAuthorize("hasAnyAuthority('ROLE_COORDINADOR', 'ROLE_PROYECTISTA')")
    @GetMapping
    public ResponseEntity<List<PlantillaDTO>> obtenerPlantillas() {
        return ResponseEntity.ok(plantillaService.obtenerPlantillasPorDespacho());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COORDINADOR', 'ROLE_PROYECTISTA')")
    public ResponseEntity<PlantillaDTO> obtenerPlantillaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(plantillaService.obtenerPorId(id));
    }
}