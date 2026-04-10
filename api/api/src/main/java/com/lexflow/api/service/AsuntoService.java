package com.lexflow.api.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lexflow.api.dto.AsuntoDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.AsuntoUsuario;
import com.lexflow.api.model.AsuntoUsuarioId;
import com.lexflow.api.model.Cliente;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.AsuntoUsuarioRepository;
import com.lexflow.api.repository.ClienteRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;
import com.lexflow.api.repository.UsuarioRepository;


import org.springframework.transaction.annotation.Transactional;


@Service

public class AsuntoService {
    

    private final AsuntoRepository asuntoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoAsuntoRepository tipoAsuntoRepository;
    private final AsuntoUsuarioRepository asuntoUsuarioRepository;

    public AsuntoService (AsuntoRepository asuntoRepository, ClienteRepository clienteRepository, TipoAsuntoRepository tipoAsuntoRepository, UsuarioRepository usuarioRepository, AsuntoUsuarioRepository asuntoUsuarioRepository){
        this.asuntoRepository = asuntoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;

        this.tipoAsuntoRepository = tipoAsuntoRepository;
        this.asuntoUsuarioRepository = asuntoUsuarioRepository;
    }


    public List<Asunto> obtenerTodos (){
        return asuntoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AsuntoDTO obtenerAsuntoPorId(Long id) {
        // Tu TenantResolver protege este findById automáticamente 🔥
        Asunto asunto = asuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: El asunto no existe o no tienes permisos para verlo"));

        return AsuntoDTO.builder()
                .id(asunto.getId())
                .titulo(asunto.getActoImpugnar()) // Usando el nombre que definiste antes
                .clienteId(asunto.getCliente().getId())
                .tipoAsuntoId(asunto.getTipoAsunto().getId())
                .camposDinamicos(asunto.getCamposDinamicos()) // 📦 Aquí va tu JSON mágico intacto
                .build();
    }

    public AsuntoDTO guardarAsunto(AsuntoDTO dto) {
        // 1. Buscamos las relaciones usando los IDs que vienen del DTO
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe"));

        TipoAsunto tipo = tipoAsuntoRepository.findById(dto.getTipoAsuntoId())
                .orElseThrow(() -> new RuntimeException("Error: El tipo de asunto no existe"));

        // 2. Construimos la Entidad nueva usando el Builder
        Asunto nuevoAsunto = Asunto.builder()
                .actoImpugnar(dto.getTitulo())
                .cliente(cliente)
                .tipoAsunto(tipo)
                .camposDinamicos(dto.getCamposDinamicos()) // 🔥 Pasamos el JSON
                
                .build();

        // 3. Guardamos en la Base de Datos
        Asunto asuntoGuardado = asuntoRepository.save(nuevoAsunto);

        // 4. Mapeamos la entidad guardada de vuelta a DTO para enviarla al Frontend
        return AsuntoDTO.builder()
                .id(asuntoGuardado.getId())
                .titulo(asuntoGuardado.getActoImpugnar())
                .clienteId(asuntoGuardado.getCliente().getId())
                .tipoAsuntoId(asuntoGuardado.getTipoAsunto().getId())
                .camposDinamicos(asuntoGuardado.getCamposDinamicos())
                .build();
    }

    
    public Optional <Asunto> actualizar(Long id, Asunto AsuntoActualizado){
        return asuntoRepository.findById(id).map(AsuntoExistente -> {
            
            
            if (AsuntoActualizado.getActoImpugnar() != null){
                AsuntoExistente.setActoImpugnar(AsuntoActualizado.getActoImpugnar());
            }

            
            return asuntoRepository.save(AsuntoExistente); 
        });
    }

    public boolean eliminar (Long id){
        asuntoRepository.deleteById(id);
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
