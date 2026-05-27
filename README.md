# JuriDesk / LexFlow — ERP Legal

Sistema de gestión jurídica SaaS para despachos de abogados. Backend en **Java 21 + Spring Boot 3.x + PostgreSQL**.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| Persistencia | Spring Data JPA + Hibernate 6 |
| Base de datos | PostgreSQL 16 |
| Seguridad | Spring Security + JWT (Auth0) |
| Almacenamiento | AWS S3 / MinIO |
| Documentación API | Springdoc OpenAPI (Swagger UI) |
| Contenedores | Docker Compose |

---

## Módulos implementados

| Módulo | Estado | Notas |
|--------|--------|-------|
| Autenticación / JWT | Funcional | Ver bugs críticos abajo |
| Gestión de asuntos (expedientes) | Funcional | DTO incompleto |
| Gestión de clientes | Funcional | — |
| Gestión de documentos (S3) | Funcional | Path traversal pendiente |
| Gestión de tareas | Funcional | Sin validaciones |
| Etapas y movimientos procesales | Funcional | Sin orquestación de flujo |
| Vencimientos y términos legales | Funcional | Sin alertas |
| Plantillas de documentos | Funcional | — |
| Gestión de usuarios y despachos | Funcional | Límites de plan no enforced |
| Multi-tenancy por despacho | Funcional | Bien implementado con `@TenantId` |
| Google Drive | Mock | No integrado |

### Endpoints disponibles

```
POST   /api/auth/login
POST   /api/auth/registro

GET    /api/asuntos
GET    /api/asuntos/{id}
POST   /api/asuntos
PUT    /api/asuntos/{id}
DELETE /api/asuntos/{id}
POST   /api/asuntos/{asuntoId}/participantes/{usuarioId}
GET    /api/asuntos/{asuntoId}/participantes

GET    /api/clientes
POST   /api/clientes
PUT    /api/clientes/{id}
DELETE /api/clientes/{id}

GET    /api/documentos
POST   /api/documentos          (multipart)
GET    /api/documentos/{id}/descargar

GET    /api/tareas
POST   /api/tareas
PUT    /api/tareas/{id}
DELETE /api/tareas/{id}

GET    /api/etapas
POST   /api/etapas
GET    /api/movimientos
POST   /api/movimientos

GET    /api/vencimientos
POST   /api/vencimientos
PUT    /api/vencimientos/{id}

GET    /api/plantillas
GET    /api/plantillas/{id}
POST   /api/plantillas
```

---

## Levantar el proyecto

```bash
# 1. Copiar variables de entorno
cp .env.example .env   # editar con tus valores

# 2. Levantar PostgreSQL
docker-compose up -d

# 3. Compilar y correr
cd api/api
./mvnw spring-boot:run
```

Swagger UI disponible en `http://localhost:8080/swagger-ui.html`

---

## Lo que está bien (no tocar)

- Arquitectura en capas limpia: Controller → Service → Repository → Model
- **Multi-tenancy con `@TenantId` de Hibernate** — cada despacho está completamente aislado
- Soft delete con `@SQLDelete` + `@SQLRestriction`
- Cascada JPA en `Asunto` (`CascadeType.REMOVE`) — los hijos se eliminan automáticamente
- **Patrón Saga compensado** en `DocumentoService` — si la BD falla después de subir a S3, el archivo se borra antes de lanzar el error
- URLs presignadas S3 con expiración de 15 minutos

---

## Bugs críticos — arreglar antes de producción

### Seguridad

**1. Dual JWT — handlers de error muertos**
`JwtAuthenticationFilter` importa `io.jsonwebtoken.*` (JJWT) pero `JwtService` usa `com.auth0.jwt.*`. Los bloques `catch(ExpiredJwtException)` nunca se ejecutan porque Auth0 lanza excepciones distintas. Un token expirado devuelve 401 vacío sin mensaje JSON.

```
Archivo: security/JwtAuthenticationFilter.java
Solución: Eliminar dependencia JJWT del pom.xml, usar solo Auth0 JWT.
```

**2. JWT secret hardcodeado**

```java
// JwtService.java:15 — visible en el repositorio
private static final String SECRET_KEY = "LexFlowSecretKeySuperSegura...";
```

Cualquiera que lea el repositorio puede forjar tokens válidos.

```
Solución: private static final String SECRET_KEY = System.getenv("JWT_SECRET");
```

**3. Path traversal en S3**
El filename del usuario se usa directamente como key de S3 sin sanitizar. Un nombre como `../../etc/passwd` puede sobrescribir archivos fuera del bucket.

```
Archivo: service/StorageService.java
Solución: String key = UUID.randomUUID() + "_" + filename.replaceAll("[^a-zA-Z0-9._-]", "_");
```

**4. RefreshToken en Body JSON**
Devolver el refresh token en el body JSON incita a guardarlo en `localStorage`, que es vulnerable a XSS.

```
Solución: Enviar refreshToken en cookie HttpOnly + Secure + SameSite=Strict.
          Dejar solo el accessToken en el body.
```

**5. IP de base de datos hardcodeada**

```properties
# application.properties:4
spring.datasource.url=jdbc:postgresql://192.168.1.200:5433/lexflow_db
```

```
Solución: spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5433/lexflow_db
```

**6. `ddl-auto=update` en producción**
Hibernate puede alterar el schema en caliente, con riesgo de pérdida de datos.

```
Solución: Migrar a Flyway o Liquibase.
          En producción usar ddl-auto=validate.
```

---

### Bugs funcionales

**7. `AsuntoDTO` incompleto**
El modelo `Asunto` tiene `estado`, `fechaLimiteLegal`, `etapaActual` y `creadoEn` pero el DTO no los mapea. Cualquier Kanban o filtro por estado en el frontend siempre recibe `null`.

```
Archivo: dto/AsuntoDTO.java + service/AsuntoService.java (método mapToDTO)
Acción: Agregar los campos faltantes al DTO y mapearlos en el servicio.
```

**8. Orden incorrecto al subir documentos**
`DocumentoService` sube el archivo a S3 **antes** de verificar que el asunto existe. Si el asunto no existe, el archivo queda huérfano en S3.

```
Archivo: service/DocumentoService.java
Solución: Buscar y validar el asunto ANTES de llamar a storageService.uploadFile().
```

**9. Sin validación en DTOs**
`LoginRequest`, `RegistroDespachoRequest` y todos los demás DTOs no tienen `@NotBlank`, `@Email` ni `@Size`. Las peticiones vacías o malformadas llegan hasta la base de datos.

```
Solución: Anotar campos en los DTOs + agregar @Valid en los parámetros de los controladores.
```

**10. Sin manejo global de excepciones**
Todos los servicios usan `throw new RuntimeException("...")`. El cliente siempre recibe HTTP 500 aunque el error sea un 404 o un 400.

```
Solución: Crear excepciones de dominio (ResourceNotFoundException, ValidationException)
          y un @RestControllerAdvice que las mapee al código HTTP correcto.
```

---

### Calidad de código

**11. Logging con `System.out.println`**
```java
// AsuntoService.java:178, 193
System.out.println("⚠️ El abogado ya estaba asignado...");
// DocumentoController.java:41
e.printStackTrace();
```
Estos mensajes van a stdout en producción sin estructura ni niveles.

```
Solución: Agregar @Slf4j en todas las clases y usar log.warn(), log.error(), etc.
```

**12. Constructor manual en `AsuntoService`**
`AsuntoService` tiene un constructor manual de 8 parámetros en lugar de `@RequiredArgsConstructor`.

**13. Sin tests**
El único archivo de test es un stub vacío (`ApiApplicationTests.java`). Cobertura: 0%.

```
Prioridad para tests: AuthService, DocumentoService (patrón Saga), AsuntoService.
```

---

## Módulos que faltan para ser un ERP legal completo

### Alta prioridad — sin esto no es un ERP

| Módulo | Por qué importa |
|--------|----------------|
| **Timesheet / Control de horas** | Los despachos facturan por hora; sin esto no se pueden generar honorarios |
| **Facturación** | Generar facturas a clientes basadas en horas trabajadas + gastos del caso |
| **Notificaciones** | Alertas de vencimientos por email/SMS — los vencimientos existen en BD pero nadie se entera |
| **Portal del cliente** | El cliente debería ver el estado de su caso sin llamar al despacho |
| **Integración de pagos** | Si es SaaS necesita cobrar planes (Stripe, Conekta, MercadoPago) |

### Media prioridad

| Módulo | Por qué importa |
|--------|----------------|
| **Calendario / Agenda** | Audiencias, citas y plazos integrados en un calendario |
| **Control de gastos** | Viáticos, honorarios de peritos, costas asociadas al caso |
| **Conflict checking** | Verificar si un cliente nuevo tiene conflicto de interés con uno existente |
| **Reportes analíticos** | Dashboard: casos abiertos, tasa de cierre, ingresos por abogado |
| **2FA** | Obligatorio para datos jurídicos sensibles |

### Baja prioridad — diferenciadores

| Módulo | Por qué importa |
|--------|----------------|
| Google Drive real | Hoy es un mock — terminar la integración |
| **Auditoría completa** | Log inmutable de quién vio o editó cada documento |
| **BPM / Flujo de trabajo** | Automatizar: "cuando se sube la sentencia, notificar al cliente y crear tarea de apelación" |
| **Base de conocimiento** | Jurisprudencia interna y precedentes del despacho |

---

## Plan de acción recomendado

### Semana 1 — Seguridad
1. Mover `JWT_SECRET` a variable de entorno
2. Eliminar JJWT del `pom.xml`, dejar solo Auth0 JWT
3. Sanitizar filenames con UUID en `StorageService`
4. Mover `refreshToken` a cookie `HttpOnly`
5. Cambiar IP hardcodeada de BD a variable de entorno

### Semana 2 — Estabilidad
1. Completar `AsuntoDTO` con los campos faltantes
2. Agregar `@NotBlank`, `@Email`, `@Size` en todos los DTOs + `@Valid` en controladores
3. Crear `GlobalExceptionHandler` con `@RestControllerAdvice`
4. Reemplazar `System.out.println` por `@Slf4j` en todas las clases
5. Reordenar `DocumentoService`: validar asunto antes de subir a S3
6. Migrar a Flyway para migraciones versionadas

### Semana 3+ — Features
1. Notificaciones de vencimientos (Spring Scheduler como punto de partida)
2. Timesheet básico vinculado a asuntos y usuarios
3. Tests unitarios para `AuthService`, `DocumentoService` y `AsuntoService`

---

## Estructura del proyecto

```
JuriDesk/
├── api/api/src/main/java/com/lexflow/api/
│   ├── controller/     (13 controladores REST)
│   ├── service/        (15 servicios de negocio)
│   ├── model/          (20 entidades JPA)
│   ├── dto/            (17 DTOs)
│   ├── repository/     (15 repositorios JPA)
│   └── security/       (JWT, filtros, multi-tenancy)
├── docker-compose.yml  (PostgreSQL 16)
└── .env                (variables de entorno — no commitear)
```
