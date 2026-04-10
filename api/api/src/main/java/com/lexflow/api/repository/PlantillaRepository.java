package com.lexflow.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lexflow.api.model.Plantilla;

public interface PlantillaRepository extends JpaRepository <Plantilla, Long> {
    List<Plantilla> findByDespachoId(Long despachoId);
}
