package com.lexflow.api.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
import com.lexflow.api.security.TenantContext;

import jakarta.transaction.Transactional;


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

    public Optional<Asunto> obtenerAsunto(Long id){
        return asuntoRepository.findById(id);
    }

    public Asunto guardarAsunto(Asunto asunto) {
        // 1. Buscamos al cliente completo en la BD usando el ID que nos mandaste
        Cliente cliente = clienteRepository.findById(asunto.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Error: El cliente no existe"));

        // 2. Buscamos el tipo de asunto completo
        TipoAsunto tipo = tipoAsuntoRepository.findById(asunto.getTipoAsunto().getId())
                .orElseThrow(() -> new RuntimeException("Error: El tipo de asunto no existe"));

        // 3. Se los inyectamos al asunto original para que ya no estén en "null"
        asunto.setCliente(cliente);
        asunto.setTipoAsunto(tipo);
        asunto.setDespachoId(TenantContext.getCurrentTenant());

        // 4. Guardamos y retornamos (ESTE return es el que le da el ID al Controller)
        return asuntoRepository.save(asunto);
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
