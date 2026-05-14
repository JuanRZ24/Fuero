package com.lexflow.api.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.lexflow.api.dto.AsuntoDTO;
import com.lexflow.api.dto.ClienteDTO;
import com.lexflow.api.dto.TipoAsuntoDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.AsuntoUsuario;
import com.lexflow.api.model.AsuntoUsuarioId;
import com.lexflow.api.model.Cliente;
import com.lexflow.api.model.Documento;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.AsuntoUsuarioRepository;
import com.lexflow.api.repository.ClienteRepository;
import com.lexflow.api.repository.TareaRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;
import com.lexflow.api.repository.UsuarioRepository;
import com.lexflow.api.repository.VencimientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class AsuntoService {
    

    private final AsuntoRepository asuntoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoAsuntoRepository tipoAsuntoRepository;
    private final AsuntoUsuarioRepository asuntoUsuarioRepository;
    private final VencimientoRepository vencimientoRepository;
    private final TareaRepository tareaRepository;
    private final StorageService storageService;

    public AsuntoService (AsuntoRepository asuntoRepository,TareaRepository tareaRepository, ClienteRepository clienteRepository,VencimientoRepository vencimientoRepository, TipoAsuntoRepository tipoAsuntoRepository, UsuarioRepository usuarioRepository, AsuntoUsuarioRepository asuntoUsuarioRepository,StorageService storageService){
        this.asuntoRepository = asuntoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.vencimientoRepository = vencimientoRepository;
        this.tipoAsuntoRepository = tipoAsuntoRepository;
        this.asuntoUsuarioRepository = asuntoUsuarioRepository;
        this.tareaRepository = tareaRepository;
        this.storageService = storageService;
    }


    private AsuntoDTO mapToDTO(Asunto asunto) {
        ClienteDTO clienteDTO = null;
        if (asunto.getCliente() != null) {
            clienteDTO = ClienteDTO.builder()
                    .id(asunto.getCliente().getId())
                    .nombre(asunto.getCliente().getNombre())
                    .email(asunto.getCliente().getEmail())
                    .telefono(asunto.getCliente().getTelefono())
                    .build();
        }

        TipoAsuntoDTO typeDTO = null;
        if (asunto.getTipoAsunto() != null) {
            typeDTO = TipoAsuntoDTO.builder()
                    .id(asunto.getTipoAsunto().getId())
                    .nombre(asunto.getTipoAsunto().getNombre())
                    .build();
        }

        return AsuntoDTO.builder()
                .id(asunto.getId())
                .actoImpugnar(asunto.getActoImpugnar())
                .cliente(clienteDTO)
                .tipoAsunto(typeDTO)
                .camposDinamicos(asunto.getCamposDinamicos())
                .build();
    }


    @Transactional(readOnly = true)
    public List<AsuntoDTO> getAll() {
        return asuntoRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AsuntoDTO getById(Long id) {
        Asunto asunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: El asunto no existe o no tienes permisos para verlo"));

        return mapToDTO(asunto);
    }

    @Transactional
    public AsuntoDTO save(AsuntoDTO dto) {
        if (dto.getClienteId() == null) {
            throw new RuntimeException("Error: El clienteId es obligatorio");
        }

        Cliente client = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe"));

        TipoAsunto type = tipoAsuntoRepository.findById(dto.getTipoAsuntoId())
                .orElseThrow(() -> new RuntimeException("Error: El tipo de asunto no existe"));

        Asunto newAsunto = Asunto.builder()
                .actoImpugnar(dto.getActoImpugnar())
                .cliente(client)
                .tipoAsunto(type)
                .camposDinamicos(dto.getCamposDinamicos())
                .build();

        Asunto savedAsunto = asuntoRepository.save(newAsunto);

        return mapToDTO(savedAsunto);
    }

    
    @Transactional
    public AsuntoDTO update(Long id, AsuntoDTO updatedDto) {
        Asunto existingAsunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Asunto no encontrado"));

        if (updatedDto.getActoImpugnar() != null) {
            existingAsunto.setActoImpugnar(updatedDto.getActoImpugnar());
        }

        if (updatedDto.getClienteId() != null) {
            Cliente client = clienteRepository.findById(updatedDto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            existingAsunto.setCliente(client);
        }

        if (updatedDto.getTipoAsuntoId() != null) {
            TipoAsunto type = tipoAsuntoRepository.findById(updatedDto.getTipoAsuntoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de Asunto no encontrado"));
            existingAsunto.setTipoAsunto(type);
        }

        Asunto savedAsunto = asuntoRepository.save(existingAsunto);
        return mapToDTO(savedAsunto);
    }

    @Transactional
    public boolean delete(Long id) {
        Asunto asunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado"));

            if (asunto.getDocumentos() != null && !asunto.getDocumentos().isEmpty()) {
            for (Documento doc : asunto.getDocumentos()) {
                try {
                    storageService.deleteFile(doc.getRutaArchivo());
                } catch (Exception e) {
                    log.error("Fallo al borrar archivo físico huérfano: {}", doc.getRutaArchivo());
                }
            }
        }

        asuntoRepository.delete(asunto);

        return true;
    }

    @Transactional
    public void addParticipant(Long asuntoId, Long usuarioId) {
        Asunto asunto = asuntoRepository.findById(asuntoId)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado con ID: " + asuntoId));

        Usuario newParticipant = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        if (asuntoUsuarioRepository.existsById_AsuntoIdAndId_UsuarioId(asuntoId, usuarioId)) {
            System.out.println("⚠️ El abogado ya estaba asignado a este expediente.");
            return;
        }

        AsuntoUsuarioId compositeId = new AsuntoUsuarioId(asuntoId, usuarioId);

        AsuntoUsuario newRelation = AsuntoUsuario.builder()
                .id(compositeId)
                .asunto(asunto)
                .usuario(newParticipant)
                .esResponsable(false)
                .build();

        asuntoUsuarioRepository.save(newRelation);
        
        System.out.println("✅ Proyectista " + newParticipant.getNombre() + " asignado al expediente " + asunto.getId());
    }

    public List<Usuario> getLegalTeam(Long asuntoId) {
        return asuntoUsuarioRepository.findById_AsuntoId(asuntoId).stream()
                .map(AsuntoUsuario::getUsuario)
                .toList();
    }

}