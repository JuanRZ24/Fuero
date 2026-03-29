package com.lexflow.api.service;

import com.lexflow.api.dto.VencimientoDTO;
import com.lexflow.api.model.*;
import com.lexflow.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VencimientoService {

    private final VencimientoRepository vencimientoRepository;
    private final AsuntoRepository asuntoRepository;
    private final EtapaProcesalRepository etapaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public VencimientoDTO.Response crear(VencimientoDTO.Request request) {
        // Obtenemos el usuario logueado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        Asunto asunto = asuntoRepository.findById(request.getAsuntoId())
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado"));

        EtapaProcesal etapa = null;
        if (request.getEtapaId() != null) {
            etapa = etapaRepository.findById(request.getEtapaId()).orElse(null);
        }

        Vencimiento nuevo = Vencimiento.builder()
                .fechaLimite(request.getFechaLimite())
                .descripcion(request.getDescripcion())
                .asunto(asunto)
                .etapa(etapa)
                .creadoPor(usuario)
                .completado(false)
                .build();

        return mapToResponse(vencimientoRepository.save(nuevo));
    }

    public List<VencimientoDTO.Response> obtenerPendientesDashboard() {
        return vencimientoRepository.findPendientesProximos().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VencimientoDTO.Response> obtenerPorAsunto(Long asuntoId) {
        return vencimientoRepository.findByAsuntoIdOrderByFechaLimiteAsc(asuntoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoCompletado(Long id) {
        Vencimiento vencimiento = vencimientoRepository.findById(id).orElseThrow();
        vencimiento.setCompletado(true);
        vencimientoRepository.save(vencimiento);
    }

    // Mapeador manual para armar el DTO de respuesta con datos útiles para React
    private VencimientoDTO.Response mapToResponse(Vencimiento v) {
        return VencimientoDTO.Response.builder()
                .id(v.getId())
                .fechaLimite(v.getFechaLimite())
                .descripcion(v.getDescripcion())
                .completado(v.isCompletado())
                .asuntoId(v.getAsunto().getId())
                .asuntoFolio("EXP-" + String.format("%04d", v.getAsunto().getId()))
                .clienteNombre(v.getAsunto().getCliente() != null ? v.getAsunto().getCliente().getNombre() : "Sin cliente")
                .etapaNombre(v.getEtapa() != null ? v.getEtapa().getNombre() : "General")
                .build();
    }
}