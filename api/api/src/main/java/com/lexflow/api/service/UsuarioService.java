package com.lexflow.api.service;

import com.lexflow.api.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.lexflow.api.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


   public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }


    public Optional <Usuario> obtenerPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public Usuario guardar(Usuario usuario) {
        
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Ya existe un usuario con este correo.");
        }

        
        String passwordEncriptado = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(passwordEncriptado);

        usuario.setActivo(true);
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
