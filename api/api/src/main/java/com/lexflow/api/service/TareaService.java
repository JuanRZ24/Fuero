package com.lexflow.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.lexflow.api.dto.TareaRequestDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.Tarea;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.TareaRepository;
import com.lexflow.api.repository.UsuarioRepository;


@Service
public class TareaService {
    

    private final TareaRepository tareaRepository;
    private final AsuntoRepository asuntoRepository;
    private final UsuarioRepository usuarioRepository;

    public TareaService(TareaRepository tareaRepository, AsuntoRepository asuntoRepository, UsuarioRepository usuarioRepository){
        this.tareaRepository = tareaRepository;
        this.asuntoRepository = asuntoRepository;
        this.usuarioRepository = usuarioRepository;
    }




    public List<Tarea> obtenerTareas(){
        return tareaRepository.findAll();
    }

    public Optional<Tarea> obtenerTarea(Long id){
        return tareaRepository.findById(id);
    }


  public Tarea crearTarea(TareaRequestDTO dto) {
        
        
        Asunto asunto = asuntoRepository.findById(dto.getAsuntoId())
                .orElseThrow(() -> new RuntimeException("Error: El asunto no existe."));

       
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("📢 ATENCIÓN: El token dice que el usuario es: [" + emailLogueado + "]");
        
        Usuario creador = usuarioRepository.findByEmail(emailLogueado)
                .orElseThrow(() -> new RuntimeException("Error: Usuario creador no encontrado en la base de datos."));

       
        Usuario asignado = usuarioRepository.findById(dto.getUsuarioAsignadoId())
                .orElseThrow(() -> new RuntimeException("Error: El usuario asignado no existe."));

        // 5. Armamos la Tarea
        Tarea nuevaTarea = Tarea.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .fechaVencimiento(dto.getFechaLimite())
                .asunto(asunto)
                .usuarioCreador(creador)     
                .usuarioAsignado(asignado)   
                .build();

        return tareaRepository.save(nuevaTarea);
    }
}




