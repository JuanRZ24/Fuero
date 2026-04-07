package com.lexflow.api.controller;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.service.DocumentoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    // 🔥 Agregamos el consumes para forzar que acepte archivos (form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> subir(
            @RequestParam("asuntoId") Long asuntoId,
            @RequestParam("etapaId") Long etapaId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            // ✅ Ahora sí pasamos TODOS los parámetros al servicio en el orden correcto
            DocumentoDTO dto = documentoService.crearYSubirDocumento(asuntoId, etapaId, archivo, descripcion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            // Imprimimos el error real en la consola de tu servidor para no quedarnos ciegos
            System.err.println("❌ Error al subir documento: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
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