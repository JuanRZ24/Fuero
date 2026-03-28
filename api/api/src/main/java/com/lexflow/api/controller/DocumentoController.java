package com.lexflow.api.controller;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.service.DocumentoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> listarTodos() {
        List<DocumentoDTO> lista = documentoService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    // --- ACTUALIZADO: Ahora recibe etapaId ---
    @PostMapping
    public ResponseEntity<DocumentoDTO> subir(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("asuntoId") Long asuntoId,
            @RequestParam("etapaId") Long etapaId, // 🔥 ¡EL NUEVO INVITADO!
            @RequestParam(value = "descripcion", required = false) String desc) {
        try {
            // Pasamos el etapaId al servicio
            DocumentoDTO dto = documentoService.subirDocumento(archivo, asuntoId, etapaId, desc);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            System.err.println("Error al subir documento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> ver(@PathVariable Long id) {
        try {
            DocumentoDTO dto = documentoService.obtenerPorId(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}