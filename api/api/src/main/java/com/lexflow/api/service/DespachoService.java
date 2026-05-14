package com.lexflow.api.service;

import org.springframework.stereotype.Service;
import com.lexflow.api.model.Despacho;
import com.lexflow.api.repository.DespachoRepository;

import lombok.Data;
import java.util.List;
import java.util.Optional;

@Service
@Data
public class DespachoService {
    

    private final DespachoRepository despachoRepository;



    public List<Despacho> getAll(){
        return despachoRepository.findAll();
    }

    public Optional<Despacho> getDespacho(Long id){
            return despachoRepository.findById(id);
    }

    public Despacho saveDespacho(Despacho despacho){
        return despachoRepository.save(despacho);
    }

}