package com.lexflow.api.controller;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.service.DocumentoService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<DocumentoDTO>> getAllDocumentos() {
        List<DocumentoDTO> lista = documentoService.getAll();
        return ResponseEntity.ok(lista);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> uploadDocument(
            @RequestParam("asuntoId") Long asuntoId,
            @RequestParam("etapaId") Long etapaId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            DocumentoDTO dto = documentoService.createAndUploadDocument(asuntoId, etapaId, archivo, descripcion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            System.err.println("❌ Error al subir documento: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> getDocumentById(@PathVariable Long id) {
        try {
            DocumentoDTO dto = documentoService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long id) {
        try {
            String url = documentoService.getDownloadUrl(id);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            System.err.println("❌ Error al generar URL: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}