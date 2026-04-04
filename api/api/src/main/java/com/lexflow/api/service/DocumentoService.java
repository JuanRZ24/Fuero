package com.lexflow.api.service;

import com.lexflow.api.dto.DocumentoDTO;
import com.lexflow.api.model.*;
import com.lexflow.api.repository.*;
import com.lexflow.api.security.TenantContext;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional; // 🔥 Importante
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final AsuntoRepository asuntoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GoogleDriveService googleDriveService;
    private final EtapaProcesalRepository etapaRepository;
    private final MovimientoProcesalRepository movimientoRepository;

    // 1. Guardar metadatos y subir a Drive
    @Transactional 
    public DocumentoDTO subirDocumento(MultipartFile archivo, Long asuntoId, Long etapaId, String desc) {
        
        // 1. Buscamos todo el contexto (como ya lo tenías)
        Asunto asunto = asuntoRepository.findById(asuntoId).orElseThrow();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario creador = usuarioRepository.findByEmail(email).orElseThrow();
        EtapaProcesal etapa = etapaRepository.findById(etapaId).orElseThrow();

        // 2. Subir a Drive
        String[] driveData = googleDriveService.subirADrive(archivo);

        // 3. Guardar el Documento
        Documento nuevoDoc = Documento.builder()
                .nombre(archivo.getOriginalFilename())
                .descripcion(desc)
                .tipoMime(archivo.getContentType())
                .tamano(archivo.getSize())
                .googleDocId(driveData[0])
                .googleDocUrl(driveData[1])
                .asunto(asunto)
                
                .creadoPor(creador)
                .etapaVinculada(etapa)
                .build();
        nuevoDoc.setDespachoId(TenantContext.getCurrentTenant());

        Documento docGuardado = documentoRepository.save(nuevoDoc);

        // ==========================================
        // 🔥 4. LA MAGIA DEL TIMELINE AUTOMÁTICO 🔥
        // ==========================================
        
        // Armamos un texto elegante para el abogado
        String textoBitacora = String.format("Se adjuntó el documento: '%s' en la etapa de %s.", 
                archivo.getOriginalFilename(), 
                etapa.getNombre());
        
        // Si el usuario escribió una descripción extra, se la pegamos al log
        if (desc != null && !desc.trim().isEmpty()) {
            textoBitacora += " Notas adicionales: " + desc;
        }

        // Creamos el movimiento (Ajusta los nombres de los campos si tu entidad los tiene diferente)
        MovimientoProcesal movimiento = MovimientoProcesal.builder()
                .titulo("Carga de Documento") // 🔥 ¡ESTA ES LA LÍNEA QUE FALTABA! 🔥
                .descripcion(textoBitacora)
                .fechaMovimiento(LocalDateTime.now()) // O LocalDate.now() si tu campo es de solo fecha
                .asunto(asunto)
                .etapaVinculada(etapa)
                .creadoPor(creador)
                .build();

        movimientoRepository.save(movimiento); // ¡Pum! Registrado en la historia.

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