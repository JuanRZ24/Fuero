package com.lexflow.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lexflow.api.model.Plantilla;

public interface PlantillaRepository extends JpaRepository <Plantilla, Long> {
    
}
