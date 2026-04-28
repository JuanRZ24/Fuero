package com.lexflow.api.repository;

import com.lexflow.api.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TareaRepository extends JpaRepository <Tarea, Long> {

    void deleteByAsuntoId(Long asuntoId);
}