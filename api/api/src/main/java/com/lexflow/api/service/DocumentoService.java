package com.lexflow.api.service;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.model.*;
import com.lexflow.api.repository.*;
import com.lexflow.api.security.TenantContext;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AsuntoRepository asuntoRepository;
    private final EtapaProcesalRepository etapaProcesalRepository;
    private final UsuarioRepository usuarioRepository;

    
    // 🔥 CAMBIO 1: Adiós Google Drive, Hola MinIO (S3)
    private final StorageService storageService;

   // 1. Guardar metadatos y subir a MinIO
    @Transactional
    public DocumentoDTO crearYSubirDocumento(Long asuntoId, Long etapaId, MultipartFile archivo, String descripcion) {
        
        // 1. Validamos que el asunto exista y pertenezca al despacho actual 
        Asunto asunto = asuntoRepository.findById(asuntoId)
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado o no pertenece a este despacho"));

        String emailAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lo buscamos en la base de datos (ya tienes el usuarioRepository inyectado)
        Usuario usuarioLogueado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        // 2. Validamos que la etapa procesal exista
        EtapaProcesal etapa = etapaProcesalRepository.findById(etapaId)
                .orElseThrow(() -> new RuntimeException("Etapa procesal no encontrada"));

        // 3. Subimos el archivo físico a MinIO
        String rutaEnMinio = storageService.subirArchivo(archivo, asuntoId);

        // 4. Creamos el registro en la base de datos
        Documento nuevoDocumento = Documento.builder()
                .nombre(archivo.getOriginalFilename()) // Usamos el nombre original por defecto
                .descripcion(descripcion)              // 🔥 Pasamos la descripción
                .rutaArchivo(rutaEnMinio)
                .fechaSubida(LocalDateTime.now())
                .asunto(asunto)
                .etapaVinculada(etapa)                  // 🔥 Lo vinculamos a su etapa
                .despachoId(TenantContext.getCurrentTenant()) // Candado Multi-tenant
                .creadoPor(usuarioLogueado)
                .build();

        // 5. Guardamos en PostgreSQL
        Documento documentoGuardado = documentoRepository.save(nuevoDocumento);
        
        // 6. Convertimos la entidad guardada a DTO para regresarla al Frontend
        return mapToDTO(documentoGuardado);
    }

    public List<DocumentoDTO> obtenerTodos() {
        List<Documento> documentos = documentoRepository.findAll();
        return documentos.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public DocumentoDTO obtenerPorId(Long id) {
        Documento doc = documentoRepository.findById(id).orElseThrow();
        return mapToDTO(doc);
    }

    // 🔥 CAMBIO 2: Limpieza del DTO para la nueva arquitectura
    private DocumentoDTO mapToDTO(Documento doc) {
        return DocumentoDTO.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .descripcion(doc.getDescripcion())
                .rutaArchivo(doc.getRutaArchivo()) // 👈 Devolvemos la ruta de S3
                // 💀 ESTOS YA ESTÁN MUERTOS, elimínalos también de tu DocumentoDTO
                // .googleDocId(doc.getGoogleDocId()) 
                // .googleDocUrl(doc.getGoogleDocUrl())
                .estadoRevision(doc.getEstadoRevision())
                .fechaSubida(doc.getFechaSubida())
                .asuntoId(doc.getAsunto().getId())
                .nombreCreador(doc.getCreadoPor() != null ? doc.getCreadoPor().getEmail() : "Sistema")
                .build();
    }



    public String ObtenerUrlDescarga(Long id){
        Documento documento = documentoRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Documento no encontrado"));
        
        return storageService.generarUrlTemporalDeDescarga(documento.getRutaArchivo());
    }
}