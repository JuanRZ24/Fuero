package com.lexflow.api.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lexflow.api.dto.AsuntoDTO;
import com.lexflow.api.dto.ClienteDTO;
import com.lexflow.api.dto.TipoAsuntoDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.AsuntoUsuario;
import com.lexflow.api.model.AsuntoUsuarioId;
import com.lexflow.api.model.Cliente;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.AsuntoUsuarioRepository;
import com.lexflow.api.repository.ClienteRepository;
import com.lexflow.api.repository.TareaRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;
import com.lexflow.api.repository.UsuarioRepository;
import com.lexflow.api.repository.VencimientoRepository;

import org.springframework.transaction.annotation.Transactional;


@Service

public class AsuntoService {
    

    private final AsuntoRepository asuntoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoAsuntoRepository tipoAsuntoRepository;
    private final AsuntoUsuarioRepository asuntoUsuarioRepository;
    private final VencimientoRepository vencimientoRepository;
    private final TareaRepository tareaRepository;

    public AsuntoService (AsuntoRepository asuntoRepository,TareaRepository tareaRepository, ClienteRepository clienteRepository,VencimientoRepository vencimientoRepository, TipoAsuntoRepository tipoAsuntoRepository, UsuarioRepository usuarioRepository, AsuntoUsuarioRepository asuntoUsuarioRepository){
        this.asuntoRepository = asuntoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.vencimientoRepository = vencimientoRepository;
        this.tipoAsuntoRepository = tipoAsuntoRepository;
        this.asuntoUsuarioRepository = asuntoUsuarioRepository;
        this.tareaRepository = tareaRepository;
    }


    private AsuntoDTO mapearA_DTO(Asunto asunto) {
        ClienteDTO clienteDTO = null;
        if (asunto.getCliente() != null) {
            clienteDTO = ClienteDTO.builder()
                    .id(asunto.getCliente().getId())
                    .nombre(asunto.getCliente().getNombre())
                    .email(asunto.getCliente().getEmail())
                    .telefono(asunto.getCliente().getTelefono())
                    .build();
        }

        TipoAsuntoDTO tipoDTO = null;
        if (asunto.getTipoAsunto() != null) {
            tipoDTO = TipoAsuntoDTO.builder()
                    .id(asunto.getTipoAsunto().getId())
                    .nombre(asunto.getTipoAsunto().getNombre())
                    .build();
        }

        return AsuntoDTO.builder()
                .id(asunto.getId())
                .actoImpugnar(asunto.getActoImpugnar())
                .cliente(clienteDTO) // Jackson sabrá que este es para el GET
                // No hace falta setear el clienteId aquí porque es WRITE_ONLY
                .tipoAsunto(tipoDTO)
                .camposDinamicos(asunto.getCamposDinamicos())
                .build();
    }

    // ==========================================
    // TUS MÉTODOS DEL SERVICIO ACTUALIZADOS
    // ==========================================

    @Transactional(readOnly = true)
    public List<AsuntoDTO> obtenerTodos() {
        // Obtenemos las entidades, las convertimos a DTO con nuestro helper y las devolvemos como lista
        return asuntoRepository.findAll().stream()
                .map(this::mapearA_DTO)
                .toList(); // Si usas Java 16+, toList() es válido. Si es Java antiguo: collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    public AsuntoDTO obtenerAsuntoPorId(Long id) {
        Asunto asunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: El asunto no existe o no tienes permisos para verlo"));

        // Reutilizamos el helper
        return mapearA_DTO(asunto);
    }

    @Transactional
    public AsuntoDTO guardarAsunto(AsuntoDTO dto) {
        // 🔥 Usamos directamente dto.getClienteId() que Jackson nos mapeó
        if (dto.getClienteId() == null) {
            throw new RuntimeException("Error: El clienteId es obligatorio");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe"));

        TipoAsunto tipo = tipoAsuntoRepository.findById(dto.getTipoAsuntoId())
                .orElseThrow(() -> new RuntimeException("Error: El tipo de asunto no existe"));

        Asunto nuevoAsunto = Asunto.builder()
                .actoImpugnar(dto.getActoImpugnar())
                .cliente(cliente)
                .tipoAsunto(tipo)
                .camposDinamicos(dto.getCamposDinamicos())
                .build();

        Asunto asuntoGuardado = asuntoRepository.save(nuevoAsunto);

        return mapearA_DTO(asuntoGuardado);
    }

    
    public Optional <Asunto> actualizar(Long id, Asunto AsuntoActualizado){
        return asuntoRepository.findById(id).map(AsuntoExistente -> {
            
            
            if (AsuntoActualizado.getActoImpugnar() != null){
                AsuntoExistente.setActoImpugnar(AsuntoActualizado.getActoImpugnar());
            }

            
            return asuntoRepository.save(AsuntoExistente); 
        });
    }

    @Transactional // IMPORTANTE: Para que si algo falla, no se borre a medias
    public boolean eliminarAsunto(Long id) {
        Asunto asunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado"));

        // 1. Matamos a los hijos primero (Se borrarán físicamente o con soft-delete si también lo tienen configurado)
        vencimientoRepository.deleteByAsuntoId(id);

        tareaRepository.deleteByAsuntoId(id);

        // 2. Matamos al padre (Se le aplicará el UPDATE de tu Soft Delete)
        asuntoRepository.delete(asunto);

        return true;
    }

    @Transactional
    public void agregarParticipante(Long asuntoId, Long usuarioId) {
        
        // 1. Validamos que el expediente y el usuario existan
        Asunto asunto = asuntoRepository.findById(asuntoId)
                .orElseThrow(() -> new RuntimeException("Expediente no encontrado con ID: " + asuntoId));

        Usuario nuevoProyectista = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // 2. Revisamos que no esté ya asignado para evitar un error de llave duplicada
        if (asuntoUsuarioRepository.existsById_AsuntoIdAndId_UsuarioId(asuntoId, usuarioId)) {
            System.out.println("⚠️ El abogado ya estaba asignado a este expediente.");
            return; // Salimos sin hacer nada
        }

        // 3. Creamos el ID Compuesto
        AsuntoUsuarioId compositeId = new AsuntoUsuarioId(asuntoId, usuarioId);

        // 4. Armamos la entidad pivote usando tu hermoso Builder
        AsuntoUsuario nuevaRelacion = AsuntoUsuario.builder()
                .id(compositeId)
                .asunto(asunto)
                .usuario(nuevoProyectista)
                .esResponsable(false) // O la lógica que decidas
                .build();

        // 5. ¡Guardamos directo en la tabla pivote!
        asuntoUsuarioRepository.save(nuevaRelacion);
        
        System.out.println("✅ Proyectista " + nuevoProyectista.getNombre() + " asignado al expediente " + asunto.getId());
    }

    public List<Usuario> obtenerEquipoLegal(Long asuntoId) {
        return asuntoUsuarioRepository.findById_AsuntoId(asuntoId).stream()
                .map(AsuntoUsuario::getUsuario)
                .toList(); // En Java 16+ puedes usar .toList() en lugar de Collectors.toList()
    }

}
