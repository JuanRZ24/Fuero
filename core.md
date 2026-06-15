# Core ERP — Núcleo multi-tenant reutilizable

Este documento describe el repositorio **como un núcleo de ERP**, ignorando el nicho legal.
El dominio legal (despachos, asuntos, etapas procesales) es solo **una plantilla** montada
sobre un motor genérico. La misma base sirve para inmobiliarias, clínicas, agencias,
talleres, consultoras, etc.: se cambia la configuración del nicho, no el núcleo.

> Para el detalle de la implementación legal y la lista de bugs, ver [`README.md`](README.md).
> Este archivo se enfoca en **qué es reutilizable** y **cómo adaptar el core a otro vertical**.

---

## 1. Qué es este core

Un backend SaaS **multi-tenant** que provee, listo para usar, los cimientos comunes a casi
cualquier ERP de gestión de "expedientes/registros":

- Aislamiento total de datos por organización (tenant).
- Una entidad central genérica de "registro" con **campos dinámicos** por nicho.
- Un motor de **tipos + plantillas + campos personalizados** para modelar cualquier vertical
  sin migraciones de base de datos.
- Pipeline de etapas y bitácora de movimientos.
- Módulos transversales: contactos/clientes, documentos (S3/MinIO), tareas, vencimientos,
  usuarios y roles, autenticación JWT.

La promesa del core: **el 70–80 % de un ERP nuevo ya está hecho**; el nicho aporta el
vocabulario, los tipos de registro, los campos y las etapas.

---

## 2. Principios de diseño

| Principio | Cómo se materializa |
|-----------|---------------------|
| **Multi-tenant por defecto** | `@TenantId` de Hibernate + `ThreadLocal` → cada query se filtra por tenant automáticamente |
| **Registro central genérico** | Una sola entidad "registro" con estado, etapa y un mapa de campos dinámicos |
| **Esquema flexible sin migraciones** | Columna **JSONB** (`camposDinamicos`) para todo lo específico del nicho |
| **Configuración, no código** | El vertical se define con tipos, plantillas y campos guardados en BD |
| **Capas limpias** | `Controller → Service → Repository → Model`, sin lógica de negocio en controladores |
| **Almacenamiento desacoplado** | Abstracción de storage (S3/MinIO) con URLs presignadas |
| **Borrado seguro** | Soft-delete (`@SQLDelete` + `@SQLRestriction`) en lugar de `DELETE` físico |

---

## 3. Arquitectura

```
HTTP ─▶ Controller ─▶ Service ─▶ Repository ─▶ PostgreSQL
                         │
                         └─▶ StorageService ─▶ S3 / MinIO

Seguridad transversal:
  JwtAuthenticationFilter  ─┐
  (extrae tenant del token) ├─▶ TenantContext (ThreadLocal)
                            │        │
                            │        ▼
                            │   LexFlowTenantResolver  ◀── Hibernate, en CADA query
                            └──────────────────────────────────────────────────────
```

**Flujo de una request autenticada**
1. `JwtAuthenticationFilter` valida el JWT y coloca el `tenantId` en `TenantContext`.
2. El controlador delega al servicio; el servicio usa repositorios JPA normales.
3. Hibernate, vía `LexFlowTenantResolver`, **inyecta el filtro de tenant en todas las
   consultas** de entidades anotadas con `@TenantId`.
4. Si no hay token/tenant, el resolver devuelve `0L` → las listas salen vacías por
   seguridad (no se filtran datos de otros tenants por error).

Archivos clave del aislamiento:
[`security/LexFlowTenantResolver.java`](api/api/src/main/java/com/lexflow/api/security/LexFlowTenantResolver.java),
[`security/TenantContext.java`](api/api/src/main/java/com/lexflow/api/security/TenantContext.java),
[`security/JwtAuthenticationFilter.java`](api/api/src/main/java/com/lexflow/api/security/JwtAuthenticationFilter.java).

---

## 4. Modelo de dominio del core

Cada entidad del core tiene un **rol genérico** independiente del nicho. La última columna
muestra cómo se llama hoy en el código (nombre legal) — eso es lo que un vertical nuevo
renombraría o re-mapearía.

| Concepto genérico (core) | Rol en cualquier ERP | Clase actual (nicho legal) |
|--------------------------|----------------------|----------------------------|
| **Tenant / Organización** | La empresa cliente del SaaS; raíz del aislamiento | `Despacho` |
| **Usuario** | Operador con rol dentro del tenant | `Usuario` + `RolUsuario` |
| **Contacto / Cliente** | Persona o empresa con la que se trabaja | `Cliente` |
| **Registro** (entidad central) | El "expediente/caso/proyecto/orden" que se gestiona | `Asunto` |
| **Tipo de registro** | Categoría configurable del registro | `TipoAsunto` |
| **Etapa / Stage** | Fase del pipeline del registro | `EtapaProcesal` |
| **Movimiento / Actividad** | Bitácora de avances del registro | `MovimientoProcesal` |
| **Vencimiento / Deadline** | Fecha límite asociada al registro | `Vencimiento` |
| **Tarea** | Pendiente asignable | `Tarea` |
| **Documento** | Archivo en object storage + metadatos | `Documento` (+ `HistorialEstadoDocumento`) |
| **Plantilla** | Definición de un formulario/registro de un nicho | `Plantilla` |
| **Campo personalizado** | Campo configurable de una plantilla | `CampoPlantilla` (`TipoCampo`) |
| **Campos dinámicos** | Valores específicos del nicho, sin columnas fijas | `Asunto.camposDinamicos` (JSONB) |

La entidad central [`model/Asunto.java`](api/api/src/main/java/com/lexflow/api/model/Asunto.java)
ya trae lo genérico de un registro: `estado`, `etapaActual`, `tipoAsunto`, `cliente`,
`creadoEn`, soft-delete y, sobre todo, el mapa `camposDinamicos`.

---

## 5. El motor de plantillas — cómo se "ajusta" un nicho

Este es el corazón de la reutilización. Permite modelar **cualquier vertical sin tocar el
schema** combinando cuatro piezas:

```
TipoAsunto ──┐
             │  (un vertical define sus tipos de registro)
Plantilla ───┤
   └─ CampoPlantilla (label, key, tipo, requerido)   ← define los campos
             │
             ▼
Asunto.camposDinamicos  (JSONB)  ← guarda los valores: { "key": valor, ... }
```

- **`TipoAsunto`** — los tipos de registro del vertical (p. ej. en inmobiliaria: "Venta",
  "Renta", "Avalúo").
- **`Plantilla` + `CampoPlantilla`** — el formulario de cada tipo. Cada campo declara
  `nombreLabel` (lo que ve el usuario), `nombreKey` (la llave en el JSONB), `tipo`
  (`TipoCampo`) y `requerido`.
  Ver [`model/CampoPlantilla.java`](api/api/src/main/java/com/lexflow/api/model/CampoPlantilla.java)
  y [`model/Plantilla.java`](api/api/src/main/java/com/lexflow/api/model/Plantilla.java).
- **`camposDinamicos`** — un `Map<String,Object>` persistido como **JSONB** en la columna
  del registro. Aquí viven todos los datos propios del nicho, **sin crear columnas ni correr
  migraciones**.

Ejemplo del mismo registro (`Asunto`) en dos verticales distintos, sin cambiar el modelo:

```jsonc
// Vertical inmobiliario
"camposDinamicos": { "m2": 120, "precio": 2500000, "tiene_estacionamiento": true }

// Vertical clínica
"camposDinamicos": { "diagnostico": "...", "alergias": "penicilina", "proxima_cita": "2026-07-01" }
```

`TipoCampo` hoy soporta `TEXTO`, `NUMERO`, `CHECKBOX`, `FECHA`
([`model/TipoCampo.java`](api/api/src/main/java/com/lexflow/api/model/TipoCampo.java)).

---

## 6. Core vs Nicho — qué se reutiliza y qué se reconfigura

| Capa | Reutilizable tal cual (CORE) | Se reconfigura por vertical (NICHO) |
|------|------------------------------|--------------------------------------|
| Multi-tenancy | ✅ Resolver, contexto, `@TenantId` | — |
| Autenticación / JWT | ✅ Filtro, servicio, refresh tokens | — |
| Entidad central de registro | ✅ Estado, etapa, soft-delete, JSONB | Campos específicos → plantilla |
| Tipos / plantillas / campos | ✅ Motor completo | Datos: qué tipos y campos existen |
| Pipeline (etapas/movimientos) | ✅ Mecanismo | Nombres y orden de las etapas |
| Documentos / storage | ✅ Saga + URLs presignadas | Categorías de documento |
| Tareas / vencimientos | ✅ CRUD y relaciones | Reglas de negocio del vertical |
| Vocabulario / labels | — | Renombrado de dominio + i18n |

---

## 7. Guía: crear un nuevo nicho desde el core

1. **Mapear el vocabulario** del vertical a las entidades core (tabla de la sección 4).
   Ej.: en un taller, `Asunto` = "Orden de servicio", `Cliente` = "Cliente", `EtapaProcesal`
   = "Estado de reparación".
2. **Definir los tipos de registro** (`TipoAsunto`) del vertical.
3. **Crear las plantillas y campos** (`Plantilla` + `CampoPlantilla`) que describen los
   datos propios del nicho → se guardarán en `camposDinamicos`.
4. **Definir las etapas del pipeline** (`EtapaProcesal`) en el orden del proceso del nicho.
5. **Sembrar todo** en el arranque desde
   [`config/DataSeederConfig.java`](api/api/src/main/java/com/lexflow/api/config/DataSeederConfig.java)
   (la costura donde hoy se inicializa el dominio).
6. **Ajustar labels / textos** para el vocabulario del vertical (ver i18n en la sección 9).

Con eso, el ERP nuevo funciona sin tocar controladores, servicios ni repositorios del core.

---

## 8. Ejemplos de verticales

| Vertical | "Registro" (`Asunto`) | Etapas (`EtapaProcesal`) | Campos dinámicos típicos |
|----------|------------------------|--------------------------|---------------------------|
| Legal | Expediente | Demanda → Pruebas → Sentencia | acto a impugnar, fecha del acto |
| Inmobiliario | Operación | Captación → Visita → Cierre | m², precio, ubicación |
| Clínica | Episodio del paciente | Admisión → Tratamiento → Alta | diagnóstico, alergias, próxima cita |
| Agencia | Proyecto de cliente | Brief → Producción → Entrega | presupuesto, deadline, canal |
| Taller | Orden de servicio | Recepción → Reparación → Entrega | placa, falla reportada, refacciones |

---

## 9. Stack y arranque

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| Persistencia | Spring Data JPA + Hibernate 6 (multi-tenant `@TenantId`) |
| Base de datos | PostgreSQL 16 (con JSONB) |
| Seguridad | Spring Security + JWT (Auth0) |
| Almacenamiento | AWS S3 / MinIO |
| Docs API | Springdoc OpenAPI (Swagger UI) |
| Contenedores | Docker Compose |

```bash
# 1. Variables de entorno
cp .env.example .env     # editar valores

# 2. Levantar PostgreSQL
docker-compose up -d

# 3. Compilar y correr
cd api/api
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 10. Qué endurecer antes de reusar el core en producción

El README ya detalla los bugs; lo que afecta al core como base reutilizable:

- **Secrets y config fuera del código**: `JWT_SECRET`, host/credenciales de BD. Hoy hay
  valores hardcodeados (incluido `docker-compose.yml` con `lexflow_*`).
- **Migraciones versionadas**: pasar de `ddl-auto=update` a Flyway/Liquibase + `validate`.
- **Validación de entrada**: `@Valid` + `@NotBlank`/`@Email`/`@Size` en los DTOs.
- **Manejo global de errores**: `@RestControllerAdvice` mapeando 400/404/409 (hoy todo es 500).
- **Sanitización de nombres de archivo** en el storage (path traversal).
- **Tests**: cobertura actual ≈ 0 %; priorizar auth, storage y la entidad central.

---

## 11. Observaciones para convertirlo en plantilla reutilizable

Hoy el core **todavía "sabe" del nicho legal** en varios puntos. Para que sirva como
plantilla limpia de cualquier ERP, conviene aplicar estos refactors. Están ordenados por
impacto.

### 11.1 Sacar los campos del nicho fuera del registro central — *(prioridad alta)*
`Asunto` tiene columnas legales que no pertenecen al core:

```java
// model/Asunto.java
private String actoImpugnar;     // legal
private LocalDate fechaActo;     // legal
private LocalDate fechaLimiteLegal; // legal
```

Estos deben vivir en `camposDinamicos` (vía plantilla), no como columnas. El registro
central genérico debería quedarse solo con: `id`, `tenantId`, `cliente`, `tipo`, `estado`,
`etapaActual`, fechas de sistema (`creadoEn`, `deletedAt`) y `camposDinamicos`.

### 11.2 Renombrar el dominio a términos genéricos — *(prioridad alta)*
Mover el nicho del **código** a los **datos**. Sugerido:

| Hoy (legal) | Core genérico |
|-------------|---------------|
| `Asunto` | `Registro` / `Record` |
| `Despacho` | `Organizacion` / `Tenant` |
| `despachoId` | `tenantId` |
| `EtapaProcesal` | `Etapa` / `Stage` |
| `MovimientoProcesal` | `Movimiento` / `Actividad` |
| `TipoAsunto` | `TipoRegistro` |
| paquete `com.lexflow` | `com.<core>` |

Tras esto, "legal" deja de ser clases y pasa a ser únicamente datos de plantilla/semilla.

### 11.3 Separar core vs vertical — *(prioridad media)*
Dividir en paquete (o módulo Maven) `core/` (reutilizable, sin nada del nicho) y `vertical/`
(semillas, labels, plantillas, reglas). `config/DataSeederConfig.java` es la costura natural:
el core no debería sembrar datos legales.

### 11.4 Definir el vertical de forma declarativa — *(prioridad media)*
Un **manifiesto de vertical** (JSON/YAML, o un seeder por perfil) que describa tipos de
registro, plantillas, campos y etapas. Cambiar de ERP = cambiar el manifiesto, sin recompilar
el core. Es la diferencia entre "plantilla" y "fork".

### 11.5 Validar `camposDinamicos` contra la plantilla — *(prioridad media)*
Hoy nada garantiza que el JSONB cumpla la definición de `CampoPlantilla` (llaves esperadas,
tipos, requeridos). Sin esta validación, el "esquema flexible" se vuelve un esquema
inexistente. Añadir un validador que contraste el mapa con la plantilla del `TipoRegistro`.

### 11.6 Ampliar `TipoCampo` — *(prioridad baja)*
Solo hay `TEXTO`, `NUMERO`, `CHECKBOX`, `FECHA`. Para cubrir más verticales conviene añadir
`SELECT` (catálogo), `RELACION` (referencia a otro registro), `ARCHIVO`, `MONEDA`,
`MULTILINEA`, etc.

### 11.7 Externalizar terminología / i18n — *(prioridad baja)*
Hay textos en español embebidos en el código (labels, mensajes). Mover los textos del nicho
a la capa de plantilla o a archivos de recursos (`messages_*.properties`) permite reusar el
core en otro idioma o vocabulario sin tocar clases.

---

### Resultado esperado
Aplicando 11.1 y 11.2, el core queda **agnóstico del nicho**: un registro genérico
multi-tenant con motor de plantillas. Con 11.3 y 11.4, montar un ERP nuevo se reduce a
escribir un manifiesto de vertical y arrancar — exactamente lo que se espera de una plantilla.
