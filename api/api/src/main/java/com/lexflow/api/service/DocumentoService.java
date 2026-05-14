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
import lombok.extern.slf4j.Slf4j; 

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AsuntoRepository asuntoRepository;
    private final EtapaProcesalRepository etapaProcesalRepository;
    private final UsuarioRepository usuarioRepository;

    private final StorageService storageService;

    @Transactional
    public DocumentoDTO createAndUploadDocument(Long asuntoId, Long etapaId, MultipartFile archivo, String descripcion) {

        String minioPath = storageService.uploadFile(archivo, asuntoId);
        
        Asunto asunto = asuntoRepository.findById(asuntoId)
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado o no pertenece a este despacho"));

        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario loggedUser = usuarioRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        EtapaProcesal etapa = etapaProcesalRepository.findById(etapaId)
                .orElseThrow(() -> new RuntimeException("Etapa procesal no encontrada"));

        try{
            
        Documento newDocument = Documento.builder()
                .nombre(archivo.getOriginalFilename()) 
                .descripcion(descripcion)              
                .rutaArchivo(minioPath)
                .fechaSubida(LocalDateTime.now())
                .asunto(asunto)
                .etapaVinculada(etapa)                  
                .despachoId(TenantContext.getCurrentTenant()) 
                .creadoPor(loggedUser)
                .build();

                Documento savedDocument = documentoRepository.saveAndFlush(newDocument);

                return mapToDTO(savedDocument);
        } catch(Exception e){
            log.error("Error al guardar en al base de datos. iniciando proceso de compensacion");
            try{
                storageService.deleteFile(minioPath);
                log.info("Compensacion exitosa: archivo {} eliminado", minioPath);
            }catch(Exception exmin){
                log.error("🚨 ALERTA CRÍTICA: Falló la BD y la compensación de S3. Archivo huérfano: {}", minioPath, exmin);
            }
            throw new RuntimeException("Error interno al procesar el documento. No se aplicaron cargos.", e);
        }
    }

    public List<DocumentoDTO> getAll() {
        List<Documento> documentos = documentoRepository.findAll();
        return documentos.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public DocumentoDTO getById(Long id) {
        Documento doc = documentoRepository.findById(id).orElseThrow();
        return mapToDTO(doc);
    }

    private DocumentoDTO mapToDTO(Documento doc) {
        return DocumentoDTO.builder()
                .id(doc.getId())
                .nombre(doc.getNombre())
                .descripcion(doc.getDescripcion())
                .rutaArchivo(doc.getRutaArchivo()) 
                .estadoRevision(doc.getEstadoRevision())
                .fechaSubida(doc.getFechaSubida())
                .asuntoId(doc.getAsunto().getId())
                .nombreCreador(doc.getCreadoPor() != null ? doc.getCreadoPor().getEmail() : "Sistema")
                .build();
    }

    public String getDownloadUrl(Long id){
        Documento document = documentoRepository.findById(id)
                    .orElseThrow(()-> new RuntimeException("Documento no encontrado"));
        
        return storageService.generateTemporaryDownloadUrl(document.getRutaArchivo());
    }
}