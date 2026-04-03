package com.lexflow.api.security;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class LexFlowTenantResolver implements CurrentTenantIdentifierResolver<Long> {

    @Override
    public Long resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getCurrentTenant();
        
        // Si hay un ID en el hilo (el usuario mandó token), lo usamos.
        // Si no hay (ej. cuando inician sesión o navegan sin token), mandamos un 0L
        // para que Hibernate filtre por ID 0 y devuelva listas vacías por seguridad.
        return tenantId != null ? tenantId : 0L;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // Esto le dice a Hibernate que se asegure de revisar el ID en cada consulta
        return true; 
    }
}