package com.lexflow.api.config;

import com.lexflow.api.model.EtapaProcesal;
import com.lexflow.api.model.TipoAsunto;
import com.lexflow.api.repository.EtapaProcesalRepository;
import com.lexflow.api.repository.TipoAsuntoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner inicializarCatalogos(TipoAsuntoRepository tipoRepo, EtapaProcesalRepository etapaRepo) {
        return args -> {
            // Solo insertamos si la tabla de Tipos de Asunto está vacía
            if (tipoRepo.count() == 0) {
                System.out.println("🌱 Sembrando catálogos iniciales en la base de datos...");

                // 1. Creamos el Tipo de Asunto Principal
                TipoAsunto contencioso = TipoAsunto.builder()
                        .nombre("Juicio Contencioso Administrativo")
                        .descripcion("Defensa legal contra actos de autoridades administrativas federales.")
                        .build();
                
                tipoRepo.save(contencioso);

                // 2. Creamos las 6 Etapas exactas que definió Ernesto
                List<EtapaProcesal> etapas = List.of(
                        EtapaProcesal.builder().nombre("Demanda").orden(1).esEtapaFinal(false).tipoAsunto(contencioso).build(),
                        EtapaProcesal.builder().nombre("Contestación").orden(2).esEtapaFinal(false).tipoAsunto(contencioso).build(),
                        EtapaProcesal.builder().nombre("Ampliación").orden(3).esEtapaFinal(false).tipoAsunto(contencioso).build(),
                        EtapaProcesal.builder().nombre("Pruebas").orden(4).esEtapaFinal(false).tipoAsunto(contencioso).build(),
                        EtapaProcesal.builder().nombre("Alegatos").orden(5).esEtapaFinal(false).tipoAsunto(contencioso).build(),
                        EtapaProcesal.builder().nombre("Sentencia").orden(6).esEtapaFinal(true).tipoAsunto(contencioso).build()
                );

                etapaRepo.saveAll(etapas);
                
                System.out.println("✅ Catálogos creados exitosamente.");
            }
        };
    }
}