# Auditoría Fuero — Reporte pre-v1.0

> ERP legal SaaS multitenant (Spring Boot + PostgreSQL). Dominio central: el **Asunto**
> (expediente legal). Esta auditoría evalúa si el sistema puede manejar datos reales de
> un despacho de forma segura.

## Resumen ejecutivo

**Veredicto: NO está listo para datos reales.** La arquitectura multitenant está bien
encaminada (Hibernate 6 `@TenantId` con discriminador, tenant resuelto del JWT en
servidor — no manipulable por el cliente), pero el aislamiento **no es uniforme**:
varias entidades sensibles (`Tarea`, `Vencimiento`) **carecen del filtro de tenant**, y
hay al menos una query que devuelve datos de todos los despachos a la vez. Para un solo
despacho el día 1 "funciona", pero el modelo de seguridad que justifica venderlo como
SaaS multitenant está roto en puntos concretos y verificables.

Además hay **secretos reales filtrados en el historial de git** (password de BD y
client-id de Google Drive), el **JWT secret está hardcodeado**, la **única capa de
autorización por rol no se ejecuta** (`@PreAuthorize` sin `@EnableMethodSecurity`), **no
hay validación de inputs ni manejo global de errores** (se filtran stack traces), y el
**ciclo de vida del Asunto está incompleto** (no existe "cambiar estado" ni "cerrar";
`update()` ignora el estado).

Lo bueno: las lecturas del propio `Asunto` (`getAll`/`getById`) sí están filtradas por
tenant; el password se guarda con `PasswordEncoder` (hash); el tenant se inyecta
server-side desde el token.

Hay que cerrar los BLOQUEANTES antes de meter un solo expediente real.

---

## Eje 1 — Aislamiento de tenant

**Cómo funciona hoy:** `JwtAuthenticationFilter` saca `despachoId` del JWT →
`TenantContext` (ThreadLocal) → `LexFlowTenantResolver` → Hibernate filtra entidades
anotadas con `@TenantId`. El tenant **se resuelve del token en servidor, no de input**
→ bien, no es manipulable por header/body. El claim va firmado en el JWT.

### [BLOQUEANTE] Entidades sensibles sin `@TenantId` → fuga entre despachos
`Tarea` (`model/Tarea.java`) y `Vencimiento` (`model/Vencimiento.java`) **no tienen
`@TenantId`**. Como el filtro de Hibernate solo aplica a entidades anotadas, cualquier
query directa a sus repositorios devuelve filas de **todos** los despachos.
- `TareaService.getTareas()` (`service/TareaService.java:35`) → `findAll()` → todas las tareas de todos los despachos.
- `TareaService.getTarea(id)` (`:39`) → `findById` lee cualquier tarea por ID.
- **Fix:** añadir `@TenantId @Column(name="despacho_id") private Long despachoId;` a `Tarea` y `Vencimiento` (igual que `Asunto`). Requiere migración: poblar `despacho_id` en filas existentes a partir del `asunto` padre antes de hacerlo `NOT NULL`.

### [BLOQUEANTE] Query que ignora el tenant en el dashboard de vencimientos
`VencimientoRepository.java:17`: `@Query("SELECT v FROM Vencimiento v WHERE v.completado = false ...")`
→ `VencimientoService.getPendingDashboard()` (`service/VencimientoService.java:48`)
devuelve **vencimientos + nombres de cliente de todos los despachos** en el panel
principal. Es la fuga más visible: el primer cliente vería deadlines y clientes de otros.
- También `getByAsunto(asuntoId)` (`:54`) y `markAsCompleted(id)` (`:60`) operan sobre `Vencimiento` por ID sin verificar pertenencia.
- **Fix:** una vez añadido `@TenantId` a `Vencimiento`, Hibernate filtra la query JPQL automáticamente. Verificar con un test de dos tenants.

### [IMPORTANTE] Catálogos compartidos sin tenant (`TipoAsunto`, `EtapaProcesal`)
No tienen `@TenantId` (`model/TipoAsunto.java`, `model/EtapaProcesal.java`); el
`DataSeederConfig` los siembra globales. Para el día 1 (un despacho) es aceptable y hasta
deseable, pero:
- En `AsuntoService.save()` (`service/AsuntoService.java:123-127`) se buscan `tipoAsunto`/`etapa` por ID sin filtro → un despacho podría asociar la etapa de otro. Hoy inocuo porque son globales; si algún día se vuelven per-despacho, se convierte en fuga.
- **Fix (decisión):** decidir explícitamente si los catálogos son globales (documéntalo y bloquea su edición por usuarios) o per-despacho (añade `@TenantId`). Recomendado: globales para v1.0.

### [IMPORTANTE] Lecturas por ID sin verificar pertenencia en relaciones
`AsuntoService.getLegalTeam(asuntoId)` (`:217`) consulta `AsuntoUsuario` (sin `@TenantId`)
directamente por `asuntoId`, sin confirmar primero que el asunto es del tenant → con un ID
ajeno revela el equipo legal de otro despacho. Mismo patrón en `HistorialEstadoDocumento`.
- **Fix:** resolver siempre el recurso raíz por su repo tenant-filtrado primero (`asuntoRepository.findById`) y derivar las relaciones desde ahí, o añadir `@TenantId` a esas entidades.

### [PUEDE ESPERAR] `Usuario.email` único global
`model/Usuario.java:46` `unique = true` global impide que dos despachos tengan el mismo
email. Día 1 irrelevante; para multitenant real debería ser único por `(despacho_id, email)`.

---

## Eje 2 — Seguridad y autenticación

### [BLOQUEANTE] Secretos reales en el historial de git
`git log` muestra el `.env` commiteado (commit previo a `3ce597f`) con:
- `DB_PASSWORD=rootpassword123`
- `GOOGLE_DRIVE_CLIENT_ID=...apps.googleusercontent.com`

Borrarlo del HEAD **no lo borra del historial**. Además `docker-compose.yml:13` tiene
`POSTGRES_PASSWORD: rootpassword123` en claro.
- **Fix:** rotar TODOS los secretos expuestos (password de BD, credenciales Google Drive, y el JWT secret de abajo). Purgar el historial (`git filter-repo`) o reescribirlo dado que el repo es chico. Mover `docker-compose` a variables de entorno. Tras rotar, los valores viejos del historial dejan de servir.

### [BLOQUEANTE] JWT secret hardcodeado
`security/JwtService.java:15`: `private static final String SECRET_KEY = "LexFlowSecretKeySuperSegura..."`.
Está en el código y en el historial. Cualquiera con el repo puede firmar tokens válidos
para **cualquier despacho** (poniendo el `despachoId` que quiera) → rompe TODO el
aislamiento de tenant.
- **Fix:** leerlo de variable de entorno (`@Value("${jwt.secret}")` o `System.getenv`), generar uno nuevo aleatorio ≥256 bits, fallar el arranque si no está presente.

### [BLOQUEANTE] `@PreAuthorize` no se ejecuta (autorización por rol inerte)
Los únicos checks de rol están en `PlantillaController.java:21,27,34`, pero **no existe
`@EnableMethodSecurity`** en ninguna config → las anotaciones son **no-ops silenciosos**.
Hoy cualquier usuario autenticado puede hacer cualquier cosa, incluido lo que parecía
restringido a coordinador. `AsuntoController` no tiene ningún check (crear/editar/borrar
abierto a cualquier autenticado).
- **Fix:** añadir `@EnableMethodSecurity` a una `@Configuration`. Revisar que los roles cubren operaciones sensibles (borrado de asuntos, gestión de usuarios). Verificar prefijo `ROLE_` vs `hasAuthority`.

### [IMPORTANTE] Refresh y logout incompletos
`AuthService` crea un `RefreshToken` en login (`service/AuthService.java:74,85`) pero **no
hay endpoint `/refresh` ni `/logout`** (`AuthController` solo tiene `/login` y `/registro`).
El access token dura 24h (`JwtService.java:26`) y al expirar el usuario debe volver a
loguearse; el refresh token guardado nunca se usa ni se revoca.
- **Fix:** implementar `/auth/refresh` (validar token no revocado/no expirado → emitir nuevo access) y `/auth/logout` (revocar). Reducir vida del access token (p.ej. 15–60 min) ahora que existe refresh.

### [IMPORTANTE] Dos librerías JWT, manejo de error muerto
`JwtAuthenticationFilter` importa excepciones de `io.jsonwebtoken` (jjwt) pero `JwtService`
usa `com.auth0.jwt`. Los `catch (ExpiredJwtException | SignatureException ...)` del filtro
**nunca se disparan**; además `JwtService.extraerEmail` traga todas las excepciones y
devuelve `null`, así que un token expirado/corrupto se trata como "anónimo" en vez de
devolver un 401 explícito.
- **Fix:** unificar en una sola lib JWT; que el filtro distinga token inválido/expirado y responda 401 con mensaje claro.

### [IMPORTANTE] Sin validación de inputs en backend
No hay `@Valid` ni anotaciones `jakarta.validation` en ningún DTO/controller. Todo se
confía al front: emails, campos obligatorios, longitudes, `camposDinamicos` (JSONB libre).
- **Fix:** añadir `spring-boot-starter-validation`, anotar DTOs (`@NotNull`, `@NotBlank`, `@Email`, `@Size`) y `@Valid` en los `@RequestBody`.

### [IMPORTANTE] CORS estático a localhost
`SecurityConfig.java:49` solo permite `localhost:5173`. En producción no funcionará y/o se
hardcodeará mal.
- **Fix:** origen permitido desde configuración/env por entorno.

### [PUEDE ESPERAR] Headers de seguridad y actuator
No se configuran headers (HSTS, etc.). `actuator` está en el pom; `anyRequest().authenticated()`
lo protege, pero conviene exponer solo `/health` explícitamente. Desactivar swagger en prod.

---

## Eje 3 — Ciclo de vida del Asunto

### [BLOQUEANTE] El ciclo de vida no está implementado end-to-end
El flujo pedido es crear → asignar → cambiar estado → cerrar. Hoy:
- **Crear:** `AsuntoService.save()` OK (estado por defecto `"ACTIVO"`, `model/Asunto.java:75`).
- **Asignar:** `addParticipant()` (`:190`) **siempre** pone `esResponsable=false` (`:209`) → no hay forma de marcar responsable; no hay reasignación ni quitar participante.
- **Cambiar estado:** **no existe**. `update()` (`:146`) actualiza `actoImpugnar`, `cliente`, `tipoAsunto`, pero **nunca toca `estado` ni `etapaActual`** aunque el DTO los trae. No hay endpoint de transición de etapa.
- **Cerrar:** **no existe** lógica de cierre. `esEtapaFinal` de `EtapaProcesal` existe pero nadie lo usa.
- **Fix:** definir una máquina de estados explícita (p.ej. `ACTIVO → SUSPENDIDO → CERRADO/ARCHIVADO`) con un endpoint `PATCH /asuntos/{id}/estado` y otro para avanzar etapa; marcar `esResponsable` correctamente al asignar.

### [IMPORTANTE] Sin validación de transiciones ni de reglas
Sin máquina de estados, hoy se puede: editar un asunto ya "cerrado", "cerrar" sin
restricción, saltar etapas, etc. `estado` es un `String` libre (`model/Asunto.java:74`) →
cualquier valor entra.
- **Fix:** convertir `estado` a enum; validar transiciones permitidas; bloquear edición/cierre de asuntos ya cerrados (idempotencia: cerrar dos veces debe ser no-op o error controlado).

### [IMPORTANTE] `update()` no valida campos dinámicos contra la plantilla
`camposDinamicos` (JSONB) se acepta tal cual sin validar contra `CampoPlantilla`
(requerido/tipo).
- **Fix:** validar el JSONB contra la definición de la plantilla del `TipoAsunto`.

---

## Eje 4 — Integridad y resiliencia de datos

### [IMPORTANTE] Sin manejo global de errores → 500 + posible stack trace
No hay `@ControllerAdvice`/`@ExceptionHandler` en todo el repo. Todos los services lanzan
`RuntimeException` genéricas; "no encontrado" devuelve **500**, no 404. Sin config de
`server.error.include-stacktrace`, Spring puede **filtrar el stack trace** en la respuesta.
`AuthService.registerNewDespacho` no maneja email duplicado → 500.
- **Fix:** `@RestControllerAdvice` que mapee excepciones a 400/404/409/500 con cuerpo JSON limpio; excepciones de dominio tipadas (`AsuntoNotFoundException`, etc.); `include-stacktrace=never`, `include-message=never` en prod.

### [IMPORTANTE] Constraints e índices de BD insuficientes
`ddl-auto` no verificable (`application.properties` no versionado — está en `.gitignore`). Riesgos:
- Falta unicidad por tenant donde corresponde; `email` es único global (ver Eje 1).
- No hay índices declarados sobre `despacho_id` (clave de todas las queries multitenant) → rendimiento degradado conforme crezcan los datos.
- FKs: las relaciones `@ManyToOne` generan FK, pero conviene fijar `ON DELETE` explícito y `nullable` coherente.
- **Fix:** introducir **migraciones versionadas (Flyway/Liquibase)** y `ddl-auto=validate` en prod (nunca `update`/`create` con datos reales). Añadir índice en `despacho_id` de cada tabla tenant.

### [IMPORTANTE] Borrado: cascada física de archivos sin transacción atómica
`AsuntoService.delete()` (`:170`) hace soft-delete del `Asunto` (`@SQLDelete`), pero
**borra físicamente los archivos en storage** dentro del mismo flujo
(`storageService.deleteFile`, `:178`). Si el soft-delete luego falla/rollback, los archivos
ya se borraron → inconsistencia. Además es soft-delete del asunto pero borrado físico
irreversible de documentos.
- **Fix:** separar el borrado físico de storage de la transacción de BD (ejecutarlo tras commit, o vía cola/evento); o mantener todo como soft-delete y limpiar storage en un job posterior.

### [PUEDE ESPERAR] `save()`/`update()` no setean campos del DTO
`fechaActo`, `fechaLimiteLegal` existen en el modelo pero `save()`/`update()` no los
mapean. Pérdida silenciosa de datos que el front pueda mandar.

---

## Eje 5 — Calidad y mantenibilidad

### [BLOQUEANTE] Cero pruebas de las rutas críticas
Solo existe `ApiApplicationTests.java` (context load). **No hay un solo test** de
aislamiento de tenant ni del ciclo de vida del asunto — justo lo que más riesgo tiene.
- **Fix mínimo para v1.0:** tests de integración con dos despachos que prueben que A no ve datos de B (asuntos, tareas, vencimientos, documentos), y un test del flujo crear→asignar→estado→cerrar con transiciones inválidas.

### [IMPORTANTE] Logging con `System.out.println` y datos sensibles
`TareaService.java:50` imprime el email del usuario por `System.out`; `AsuntoService` usa
`System.out` (`:199,214`). Logs no estructurados; se filtra PII.
- **Fix:** usar SLF4J (ya está `@Slf4j` en algunas clases) en niveles adecuados, sin volcar PII.

### [PUEDE ESPERAR] Devolver entidades JPA en vez de DTOs
`AsuntoController.getLegalTeam` devuelve `List<Usuario>` (entidad con `passwordHash`,
relaciones lazy); `TareaService` devuelve `Tarea`. Riesgo de serializar de más /
`LazyInitializationException`.
- **Fix:** DTOs en todas las respuestas (ya se hace en Asunto/Vencimiento; falta en Usuario/Tarea).

### [PUEDE ESPERAR] Healthcheck y limpieza
Actuator presente pero sin exposición explícita; comentarios emoji y nombres mezclados
ES/EN. Cosmético.

---

## Qué hacer primero — ruta a v1.0

**Fase 0 — BLOQUEANTES de seguridad (antes de cualquier dato real):**
1. **Rotar todos los secretos** filtrados (BD, Google Drive, JWT) y purgar/asumir comprometido el historial git. Mover `docker-compose` y JWT secret a variables de entorno; fallar el arranque si faltan.
2. **Cerrar las fugas de tenant:** añadir `@TenantId` a `Tarea` y `Vencimiento` (+ migración de `despacho_id`); verificar que `getPendingDashboard`/`getTareas` quedan filtradas. Resolver relaciones siempre desde el recurso raíz tenant-filtrado.
3. **Activar la autorización:** añadir `@EnableMethodSecurity` y poner checks de rol en operaciones sensibles de `AsuntoController` (crear/editar/borrar) y gestión de usuarios.
4. **Tests de aislamiento de tenant (dos despachos)** que prueben que las fugas anteriores quedaron cerradas. No cerrar la Fase 0 sin estos en verde.

**Fase 1 — Core funcional + resiliencia:**
5. Implementar el ciclo de vida del Asunto: enum de estados + máquina de transiciones, endpoint cambiar estado / avanzar etapa, cierre con `esEtapaFinal`, marcar responsable real, bloquear edición de cerrados. Tests del flujo.
6. `@RestControllerAdvice` global + excepciones de dominio tipadas + `include-stacktrace=never`.
7. Validación de inputs (`@Valid` + jakarta.validation) en todos los DTOs.
8. Refresh + logout funcionando; reducir vida del access token; unificar librería JWT.

**Fase 2 — Integridad y operación:**
9. Migraciones versionadas (Flyway), `ddl-auto=validate`, índices en `despacho_id`, unicidad por tenant.
10. Arreglar el borrado de storage fuera de la transacción de BD.
11. CORS por entorno, headers de seguridad, swagger off en prod, logging SLF4J sin PII.

**Puede esperar post-v1.0:** email único por tenant, catálogos per-despacho, DTOs
faltantes (Usuario/Tarea), validación de `camposDinamicos` contra plantilla, mapear
`fechaActo`/`fechaLimiteLegal`.
