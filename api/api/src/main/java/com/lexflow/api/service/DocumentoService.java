package com.lexflow.api.service;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.model.*;
import com.lexflow.api.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AsuntoRepository asuntoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GoogleDriveService googleDriveService;
    private final EtapaProcesalRepository etapaRepository;

    // 1. Guardar metadatos y subir a Drive
    public DocumentoDTO subirDocumento(MultipartFile archivo, Long asuntoId, Long etapaId, String desc) {
    // Buscamos el asunto y el usuario (como ya lo hacías)
    Asunto asunto = asuntoRepository.findById(asuntoId).orElseThrow();
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Usuario creador = usuarioRepository.findByEmail(email).orElseThrow();

    // 🔥 BUSCAMOS LA ETAPA SELECCIONADA POR ERNESTO 🔥
    EtapaProcesal etapa = etapaRepository.findById(etapaId).orElseThrow();

    // Subir a Drive (bypass)
    String[] driveData = googleDriveService.subirADrive(archivo);

    // Creamos la entidad incluyendo la etapa_vinculada
    Documento nuevoDoc = Documento.builder()
            .nombre(archivo.getOriginalFilename())
            .descripcion(desc)
            .tipoMime(archivo.getContentType())
            .tamano(archivo.getSize())
            .googleDocId(driveData[0])
            .googleDocUrl(driveData[1])
            .asunto(asunto)
              .creadoPor(creador)
            .etapaVinculada(etapa) // 👈 AQUÍ SE LIGA AL "CAJÓN" CORRECTO
            .build();

    Documento docGuardado = documentoRepository.save(nuevoDoc);
    return mapToDTO(docGuardado);
}

    public List<DocumentoDTO> obtenerTodos() {
        // 1. Buscamos todos los registros en la base de datos
        List<Documento> documentos = documentoRepository.findAll();
        
        // 2. Usamos Streams de Java para convertir cada Documento a DTO mágicamente
        return documentos.stream()
                .map(this::mapToDTO) // Llama a mapToDTO() por cada elemento de la lista
                .toList();           // Lo vuelve a empaquetar en una lista nueva
    }

    // 2. Obtener un documento por ID para el visualizador
    public DocumentoDTO obtenerPorId(Long id) {
        Documento doc = documentoRepository.findById(id).orElseThrow();
        return mapToDTO(doc);
    }

    // Mapper manual (para no meter más librerías)
    private DocumentoDTO mapToDTO(Documento doc) {
        return DocumentoDTO.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .descripcion(doc.getDescripcion())
                .googleDocId(doc.getGoogleDocId())
                .googleDocUrl(doc.getGoogleDocUrl())
                .estadoRevision(doc.getEstadoRevision())
                .fechaSubida(doc.getFechaSubida())
                .asuntoId(doc.getAsunto().getId())
                .nombreCreador(doc.getCreadoPor().getEmail()) // O el nombre real si lo tienes
                .build();
    }
}