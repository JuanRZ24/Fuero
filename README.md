# Lexflow Backend - Análisis de Sistema y Plan de Mejoras

Este documento presenta un análisis de las funcionalidades actuales, la arquitectura y las áreas que requieren mejoras, tanto a nivel de nuevas características como de calidad de código y seguridad para el proyecto **Lexflow Backend**.

## 🚀 Funcionalidades Actuales
El sistema es una plataforma de gestión jurídica estructurada en capas (Controller, Service, Repository, Model, DTO) y cuenta con las siguientes características clave:
1. **Seguridad y Autenticación:** Basada en JWT con soporte para Refresh Tokens.
2. **Gestión de Asuntos y Clientes:** CRUD de expedientes legales y perfiles de clientes.
3. **Seguimiento Procesal:** Etapas y movimientos procesales de cada caso.
4. **Gestión de Tareas:** Asignaciones de tareas vinculadas a asuntos.
5. **Gestión de Documentos:** Carga y administración de estados de documentos (S3/Google Drive).
6. **Multi-Tenancy:** Separación de datos por despacho (`TenantId`), permitiendo su uso como SaaS.

---

## 🔍 Auditoría de Código Avanzada (Transacciones y Lógica) (NUEVO)

Tras una revisión profunda (línea por línea) del código fuente en los Servicios críticos y Filtros de Seguridad, se encontraron graves errores lógicos y de manejo de transacciones:

1. **Riesgo en Transacciones Distribuidas (DocumentoService)**
   - **Problema:** En el método `crearYSubirDocumento`, primero se sube el archivo a MinIO (`storageService.subirArchivo`) y *después* se guarda la entidad en PostgreSQL.
   - **Riesgo:** Si falla el guardado en la base de datos (por ejemplo, porque un campo es nulo o excede el tamaño), la transacción de base de datos hace *rollback*, pero el archivo físico ya fue subido a S3/MinIO, quedando "huérfano" para siempre, generando costos de almacenamiento basura.
   - **Solución:** Implementar un patrón de compensación (Saga/Outbox) o, como mínimo, capturar la excepción de la BD y mandar a borrar el archivo en S3 antes de lanzar el error hacia arriba.

2. **Ausencia de Transaccionalidad Crítica (AuthService)**
   - **Problema:** El método `login(LoginRequest request)` no tiene la anotación `@Transactional`, pero realiza operaciones de escritura (`crearRefreshTokenParaUsuario` hace un `save()`).
   - **Riesgo:** Si hay un error de concurrencia o la base de datos se satura después de actualizar el *Refresh Token*, la base de datos puede quedar en un estado inconsistente. Todas las operaciones mixtas de lectura/escritura deben ser transaccionales.

3. **Borrado en Cascada Manual y Riesgoso (AsuntoService)**
   - **Problema:** El método `eliminarAsunto(Long id)` borra dependencias manualmente (vencimientos y tareas) antes de borrar el Asunto.
   - **Riesgo:** Esto es un anti-patrón de JPA. Obliga al desarrollador a recordar cada nueva tabla hija que se cree a futuro, provocando errores de restricción de llave foránea (Foreign Key) si se le olvida.
   - **Solución:** Delegar esto a JPA usando `CascadeType.REMOVE` o, idealmente, la anotación `@SQLDelete` a nivel de Entidad junto a propiedades `onDelete="CASCADE"` en la base de datos.

4. **Excepciones Silenciadas en Seguridad (JwtAuthenticationFilter)**
   - **Problema:** El filtro `doFilterInternal` atrapa las excepciones globales (try-catch genérico) y hace un simple `System.err.println()`, luego permite que la petición continúe hacia el controlador llamando a `filterChain.doFilter(request, response);`.
   - **Riesgo:** Si un token está malformado o un usuario intenta inyectar un payload corrupto, en lugar de recibir un HTTP 401/403 inmediato y detener el flujo, la petición sigue viajando vacía hacia los controladores, donde fallará con un NullPointerException y devolverá un HTTP 500.

5. **Excepción de Lazy Initialization (AsuntoService)**
   - **Problema:** El método `obtenerEquipoLegal` busca datos relacionales a través de `AsuntoUsuario::getUsuario` pero el método carece de `@Transactional(readOnly = true)`.
   - **Riesgo:** Al no existir una transacción abierta, cuando JPA intente resolver el "Usuario" mapeado con `FetchType.LAZY` (proxy de Hibernate), lanzará un clásico `LazyInitializationException`, tirando abajo la petición.

---

## 🏗️ Mejoras Necesarias: Arquitectura y Rendimiento JPA

1. **Exposición Directa de Entidades (Fuga de Abstracción)**
   - **Problema:** En varios Controladores y Servicios se están devolviendo directamente las Entidades JPA (`Cliente`, `Despacho`, `Usuario`, `Tarea`) en lugar de DTOs.
   - **Riesgo:** Exponer entidades revela la base de datos al cliente y puede provocar ciclos infinitos de serialización JSON.

2. **Rendimiento de Base de Datos (Problema N+1 y FetchTypes)**
   - **Problema:** Existen relaciones que carecen del parámetro explícito de fetch y pueden estar usando `EAGER` fetching por defecto o causando problemas de `N+1 select`.

3. **Validación de Datos (DTOs)**
   - **Problema:** Las peticiones (ej. `RegistroDespachoRequest`, `LoginRequest`) no usan `spring-boot-starter-validation` (`@NotBlank`, `@Email`, etc.).

4. **Mapeo Automático de Entidades y DTOs**
   - **Solución:** Implementar **MapStruct** para automatizar la conversión bidireccional.

5. **Manejo Centralizado de Excepciones**
   - **Solución:** Implementar un `@RestControllerAdvice` para devolver siempre un JSON estandarizado con el código HTTP correspondiente.

---

## 💻 Mejoras Necesarias: Calidad de Código

1. **Implementación de Pruebas Automatizadas (Testing)**
   - Iniciar suite con **JUnit 5** y **Mockito** para Servicios.
2. **Estandarización del Idioma (Evitar Spanglish)**
   - Migrar todo el código fuente al **inglés**.
3. **Corrección de Dependencias (POM.xml)**
   - Ajustar `spring-boot-starter-parent` a la versión `3.4.1` (actualmente está en `4.0.4`, que no existe).
4. **Sistema de Logs Estructurado**
   - Utilizar `@Slf4j` y evitar `System.out.println()`.

---

## 🛡️ Mejoras Necesarias: Seguridad (Prioridad Crítica)

1. **Gestión de Secretos y Credenciales**
   - **Problema:** Contraseñas de Base de Datos, S3, y la `SECRET_KEY` escritas en texto plano.
   - **Solución Inmediata:** Usar variables de entorno y rotar contraseñas comprometidas.

2. **Configuración de CORS Insegura**
   - **Problema:** Existen controladores que utilizan `@CrossOrigin(origins = "*")` sobrescribiendo la seguridad global.

3. **Vulnerabilidad XSS en Entrega de Tokens JWT**
   - **Solución:** Configurar el backend para enviar el *Refresh Token* obligatoriamente a través de una cookie `HttpOnly` y `Secure`.