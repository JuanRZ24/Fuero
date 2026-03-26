package com.lexflow.api.service;

import com.lexflow.api.model.Usuario;
import org.springframework.stereotype.Service;

import com.lexflow.api.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    

    private final UsuarioRepository usuarioRepository;


    public UsuarioService (UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }


    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }


    public Optional <Usuario> obtenerPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public Usuario guardar(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> actualizar(Long id, Usuario usuarioActualizado){
        
        return usuarioRepository.findById(id).map(userExistente -> {
            
            
            if (usuarioActualizado.getNombre() != null){
                userExistente.setNombre(usuarioActualizado.getNombre());
            }
            if (usuarioActualizado.getRol() != null){
                userExistente.setRol(usuarioActualizado.getRol());
            }
            if (usuarioActualizado.getActivo() != null){
                userExistente.setActivo(usuarioActualizado.getActivo());
            }

            
            return usuarioRepository.save(userExistente); 
        });
    }

    public boolean eliminar(Long id){
        
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
