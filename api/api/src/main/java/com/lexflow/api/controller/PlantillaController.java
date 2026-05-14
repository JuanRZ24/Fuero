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
@SecurityRequirement(name = "bearerAuth") 
public class PlantillaController {

    private final PlantillaService plantillaService;

    @PreAuthorize("hasAuthority('ROLE_COORDINADOR')") 
    @PostMapping
    public ResponseEntity<PlantillaDTO> createPlantilla(@RequestBody PlantillaDTO dto) {
        return ResponseEntity.ok(plantillaService.createPlantilla(dto));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_COORDINADOR', 'ROLE_PROYECTISTA')")
    @GetMapping
    public ResponseEntity<List<PlantillaDTO>> getPlantillas() {
        return ResponseEntity.ok(plantillaService.getPlantillasByDespacho());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_COORDINADOR', 'ROLE_PROYECTISTA')")
    public ResponseEntity<PlantillaDTO> getPlantillaById(@PathVariable Long id) {
        return ResponseEntity.ok(plantillaService.getById(id));
    }
}