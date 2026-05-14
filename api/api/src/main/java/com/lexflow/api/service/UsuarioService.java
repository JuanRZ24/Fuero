package com.lexflow.api.service;

import com.lexflow.api.model.RolUsuario;
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


    public List<Usuario> getAll(){
        return usuarioRepository.findAll();
    }


    public Optional <Usuario> getById(Long id){
        return usuarioRepository.findById(id);
    }

    public Usuario save(Usuario usuario) {
        
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Ya existe un usuario con este correo.");
        }

        
        String encryptedPassword = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(encryptedPassword);

        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> update(Long id, Usuario updatedUser){
        
        return usuarioRepository.findById(id).map(existingUser -> {
            
            if (updatedUser.getNombre() != null){
                existingUser.setNombre(updatedUser.getNombre());
            }
            if (updatedUser.getRol() != null){
                existingUser.setRol(updatedUser.getRol());
            }
            if (updatedUser.getActivo() != null){
                existingUser.setActivo(updatedUser.getActivo());
            }

            return usuarioRepository.save(existingUser); 
        });
    }

    public boolean delete(Long id){
        
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }


    public List<Usuario> getByRole(RolUsuario rol) {
        return usuarioRepository.findByRol(rol); 
    }
}