# Backend Lexflow — Lista de lo que falta implementar

> Generado tras analizar el backend (`api/`) contra cada pantalla del frontend (`LexflowFront`).
> Ordenado por prioridad. Marca **[BLOQUEA FRONT]** cuando una pantalla ya construida no funciona por esto.

---

## 🔴 Prioridad ALTA (rompen funcionalidad ya visible en el front)

### 1. `AsuntoDTO` / `AsuntoService.mapToDTO` está incompleto **[BLOQUEA FRONT]**
El `mapToDTO` solo devuelve `id`, `actoImpugnar`, `cliente`, `tipoAsunto` y `camposDinamicos`.
**No expone** campos que el modelo `Asunto` SÍ tiene y que el front ya consume:
- `estado` → en `CaseDetail` siempre se ve "ACTIVO" aunque cambie en BD.
- `etapaActual` → el **Kanban del Dashboard** agrupa por `asunto.etapaActual.nombre`; como nunca llega, **todos los expedientes caen en "Sin Etapa"**.
- `fechaLimiteLegal` → "Casos Atascados" del Dashboard filtra por `!a.fechaLimiteLegal`; como nunca llega, **todos aparecen atascados**.
- `fechaActo`, `creadoEn`.
- `descripcion` → el DTO declara el campo pero nunca se llena ni se guarda.

**Acción:** mapear estos campos en `mapToDTO` (y crear `EtapaProcesalDTO` ligero para `etapaActual`).

### 2. `AsuntoService.update` ignora `estado` **[BLOQUEA FRONT]**
`CaseDetail` envía `estado` en el `PUT /api/asuntos/{id}`, pero `update()` solo procesa `actoImpugnar`, `clienteId` y `tipoAsuntoId`. **Editar el estado del expediente no persiste.**
**Acción:** añadir `if (dto.getEstado() != null) existing.setEstado(...)`. Idealmente también `etapaActual` y `fechaLimiteLegal`.

### 3. `AsuntoService.save` no asigna etapa inicial
Al crear un asunto no se setea `etapaActual` (ni `fechaActo`, `fechaLimiteLegal`). Por eso el Kanban nace vacío de etapas.
**Acción:** asignar la primera `EtapaProcesal` del `TipoAsunto` como etapa inicial al crear.

### 4. Tareas: faltan UPDATE / cambio de estado / subtareas **[BLOQUEA FRONT]**
`TareaController` solo tiene `GET` (todas / por id) y `POST`. En `TareaDetalle.jsx`:
- Botón **"Editar"** y **"Marcar Completada"** no tienen endpoint → falta `PUT /api/tareas/{id}` y/o `PATCH /api/tareas/{id}/estado`.
- El modelo `Tarea` **no tiene `prioridad`** y el front la muestra (`tarea.prioridad`).
- El front usa **`tarea.subtareas`** (checklist) → no existe ni modelo ni endpoints de subtareas.
- El `GET` devuelve la entidad `Tarea` cruda: `asunto` está `@JsonIgnore` y `usuarioAsignado` es objeto, pero el front espera `asignadoA` (string), `expedienteId`, `expedienteNombre`, `rolAsignado`. **Falta un `TareaResponseDTO`** que aplane estos datos.
- Falta filtro por estado para el tablero de `Tasks.jsx` (`GET /api/tareas?estado=`).
- Falta `DELETE /api/tareas/{id}`.

### 5. `DocumentoDTO` no expone campos que el front usa **[BLOQUEA FRONT]**
El front (`Dashboard`, `CaseDetail`, `DocumentViewer`) usa `doc.tipoMime`, `doc.googleDocUrl`, `doc.creadoPor.nombre`, `doc.etapaNombre`, pero el DTO solo trae `nombre`, `descripcion`, `rutaArchivo`, `estadoRevision`, `fechaSubida`, `asuntoId`, `nombreCreador`.
**Acción:** añadir al DTO `tipoMime`, `urlDescarga`/`googleDocUrl`, `etapaId` + `etapaNombre`, y unificar `nombreCreador` vs `creadoPor`.

---

## 🟠 Prioridad MEDIA (endpoints faltantes para pantallas existentes)

### 6. Configuración del Despacho — falta `PUT` **[BLOQUEA FRONT]**
`DespachoController` solo tiene `GET`, `GET/{id}`, `POST`. La pantalla **`FirmSettings` → "Perfil de la Firma"** (nombre, dirección, teléfono, logo) **no puede guardar**.
**Acción:** `PUT /api/despachos/{id}` + subida de logo. Añadir campos `direccion`, `telefono`, `logoUrl` al modelo `Despacho` si no existen.

### 7. Perfil de usuario (`Profile.jsx`) sin soporte **[BLOQUEA FRONT]**
No hay `GET /api/usuarios/me` ni endpoint para que el usuario edite su propio perfil. El modelo `Usuario` no parece tener `telefono`, `fotoUrl`, `firmaUrl` ni preferencias de notificaciones, todos usados en la pantalla.
**Acción:** `GET /api/usuarios/me`, `PUT /api/usuarios/me`, campos nuevos en `Usuario`, y subida de foto/firma.

### 8. Endpoint de asuntos por cliente (optimización)
No existe `GET /api/clientes/{id}/asuntos` ni `GET /api/asuntos?clienteId=`.
> El front (`ClienteDetalle`, ya implementado) hoy descarga **todos** los asuntos y filtra en cliente. Funciona, pero no escala.
**Acción:** endpoint dedicado filtrando por `cliente_id` (respetando tenant).

### 9. Endpoint de documentos por asunto (optimización)
`GET /api/documentos` devuelve todos; no hay `GET /api/documentos?asuntoId=` ni `/asunto/{id}`.
> El front (`CaseDetail`, ya implementado) descarga todos y filtra en cliente.
**Acción:** filtro por asunto y por etapa. Falta también `DELETE /api/documentos/{id}`.

### 10. Estados e historial de documentos sin endpoints
Existen los modelos `EstadoDoc` e `HistorialEstadoDocumento`, pero no hay endpoints para **cambiar el estado** (BORRADOR → FINAL → FIRMADO) ni para **ver el historial**. `DocumentViewer.jsx` lo necesita.
**Acción:** `PATCH /api/documentos/{id}/estado` y `GET /api/documentos/{id}/historial`.

### 11. Plantillas: faltan UPDATE / DELETE
`PlantillaController` solo tiene `POST`, `GET`, `GET/{id}`. `GestionPlantillas` / `PlantillaDetalle` necesitan editar y eliminar.
**Acción:** `PUT /api/plantillas/{id}`, `DELETE /api/plantillas/{id}`.

### 12. Etapas procesales: solo lectura
`EtapaProcesalController` solo tiene `GET`. No hay forma de **configurar** etapas por tipo de asunto (crear/editar/ordenar/eliminar). `FirmSettings` debería gestionarlas junto a los Tipos de Asunto.
**Acción:** CRUD completo de etapas asociadas a un `TipoAsunto`.

---

## 🟡 Prioridad BAJA / mejoras

### 13. Auth: falta refresh token y logout
`AuthController` solo tiene `/login` y `/registro`. Existen `RefreshToken` (modelo + repo) pero **no hay `POST /api/auth/refresh` ni logout/revoke**. El front guarda `accessToken` y al expirar bota al login sin renovar.
**Acción:** `POST /api/auth/refresh`, `POST /api/auth/logout`. Añadir lógica de refresh en el front (interceptor).

### 14. Movimientos procesales: faltan UPDATE / DELETE
`MovimientoProcesalController` solo `GET` por asunto y `POST`. Sin editar/eliminar movimientos de la bitácora.

### 15. Vencimientos: faltan DELETE y "descompletar"
Solo `POST`, `GET /pendientes`, `GET /asunto/{id}`, `PUT /{id}/completar`. Falta eliminar y revertir.

### 16. Búsqueda global
La barra "Buscar archivos..." del `Layout` no tiene endpoint de búsqueda transversal (asuntos/clientes/documentos).

### 17. Almacenamiento en modo mock
`GoogleDriveService` está en modo mock (según `SISTEMA_LEXFLOW.md`). Confirmar si producción usa `S3Config` y documentar/cerrar el mock.

### 18. Paginación y orden
Todos los `GET` de listas (`asuntos`, `clientes`, `documentos`, `tareas`) devuelven todo sin paginar. A medida que crezcan los datos conviene `Pageable`.

---

## Resumen rápido por controller

| Controller | Tiene | Le falta |
| :--- | :--- | :--- |
| Auth | login, registro | **refresh, logout** |
| Asuntos | CRUD, participantes | **DTO completo (estado/etapa/fechas), update de estado, etapa inicial, filtro por cliente** |
| Clientes | CRUD | filtro/relación de asuntos |
| Tareas | getAll, getById, create | **update, cambiar estado, delete, DTO aplanado, prioridad, subtareas, filtro por estado** |
| Documentos | getAll, upload, getById, descargar | **filtro por asunto/etapa, delete, cambio de estado, historial, DTO completo** |
| Despachos | getAll, getById, registro | **PUT (perfil de firma), logo** |
| Usuarios | CRUD, por rol | **/me (get y put), foto, firma, preferencias** |
| Tipo Asunto | CRUD ✅ | — (completo) |
| Etapas | getAll, por tipo | **CRUD de configuración** |
| Movimientos | por asunto, create | update, delete |
| Vencimientos | create, pendientes, por asunto, completar | delete, descompletar |
| Plantillas | create, getAll, getById | **update, delete** |
