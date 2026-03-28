package com.lexflow.api.service;

import com.lexflow.api.dto.MovimientoProcesalDTO;
import com.lexflow.api.model.Asunto;
import com.lexflow.api.model.MovimientoProcesal;
import com.lexflow.api.model.Usuario;
import com.lexflow.api.repository.AsuntoRepository;
import com.lexflow.api.repository.MovimientoProcesalRepository;
import com.lexflow.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoProcesalService {

    private final MovimientoProcesalRepository movimientoRepository;
    private final AsuntoRepository asuntoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<MovimientoProcesalDTO> obtenerHistorial(Long asuntoId) {
        List<MovimientoProcesal> movimientos = movimientoRepository.findByAsuntoIdOrderByFechaMovimientoDesc(asuntoId);
        
        // Mapeamos de Entidad a DTO
        return movimientos.stream().map(mov -> MovimientoProcesalDTO.builder()
                .id(mov.getId())
                .titulo(mov.getTitulo())
                .descripcion(mov.getDescripcion())
                .fechaMovimiento(mov.getFechaMovimiento())
                .fechaVencimiento(mov.getFechaVencimiento())
                .asuntoId(mov.getAsunto().getId())
                .nombreCreador(mov.getCreadoPor().getEmail())
                .documentoId(mov.getDocumento() != null ? mov.getDocumento().getId() : null)
                .nombreDocumento(mov.getDocumento() != null ? mov.getDocumento().getNombre() : null)
                .build()
        ).toList();
    }

    @Transactional
    public void crearMovimiento(MovimientoProcesalDTO request, String emailUsuario) {
        Usuario creador = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        Asunto asunto = asuntoRepository.findById(request.getAsuntoId()).orElseThrow();

        MovimientoProcesal nuevoMovimiento = MovimientoProcesal.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .fechaVencimiento(request.getFechaVencimiento())
                .asunto(asunto)
                .creadoPor(creador)
                .build();

        movimientoRepository.save(nuevoMovimiento);
    }
}