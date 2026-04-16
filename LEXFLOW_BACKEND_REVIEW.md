# Lexflow Backend - Architectural Review & Code Analysis

Este documento contiene un análisis exhaustivo del repositorio `Lexflow-backend`, actuando desde la perspectiva de un Arquitecto de Software Senior. Se identifican vulnerabilidades de seguridad, problemas arquitectónicos, inconsistencias en el código y los pasos recomendados para solucionarlos.

---

## 🚨 1. Fallas de Seguridad Críticas (Security Flaws)

### 1.1. Secretos y Credenciales Hardcodeadas (`application.properties`)
- **Problema:** El archivo `application.properties` contiene múltiples secretos en texto plano que jamás deberían estar en el control de versiones (Git):
  - Contraseñas de Base de Datos (`rootpassword123`).
  - Credenciales de AWS S3 (`adminLexFlow`, `SuperSecretPassword123`).
  - Google Drive Client Secret (`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`) y Refresh Token.
- **Riesgo:** Cualquier persona con acceso al repositorio o que intercepte el código fuente puede comprometer la base de datos, el bucket S3 y la cuenta de Google Drive.
- **Solución:** Migrar inmediatamente todas las credenciales a variables de entorno (`System.getenv()`) usando herramientas como **Spring Boot Profiles** con archivos `.env` (ej. usando `spring-dotenv`), y rotar (invalidar) las credenciales actuales expuestas.

### 1.2. Inconsistencia en la Configuración de CORS
- **Problema:** `SecurityConfig.java` centraliza correctamente el CORS para los puertos `5173` (Vite). Sin embargo, hay controladores (`VencimientoController`, `EtapaProcesalController`) que usan la anotación `@CrossOrigin(origins = "*")`.
- **Riesgo:** Esta anotación sobrescribe la configuración de seguridad global, abriendo esos endpoints específicos a solicitudes de **cualquier origen** y exponiéndolos a ataques CSRF/XSS de sitios maliciosos.
- **Solución:** Eliminar completamente la anotación `@CrossOrigin` de todos los controladores y confiar exclusivamente en la configuración centralizada de `SecurityFilterChain`.

### 1.3. Almacenamiento y Transmisión de Tokens (JWT)
- **Problema:** En `AuthService.java` (y `AuthController`), el `refreshToken` y el `token` de acceso se devuelven en el cuerpo (Body) de la respuesta JSON.
- **Riesgo:** Esto incentiva que el frontend almacene ambos tokens en `localStorage`, haciéndolos extremadamente vulnerables a ataques de Cross-Site Scripting (XSS).
- **Solución:** Enviar el **Refresh Token** en una cookie `HttpOnly`, `Secure` y `SameSite=Strict`. El Access Token puede ir en memoria o también en otra cookie `HttpOnly`.

### 1.4. Manejo de Errores Silencioso o Inseguro
- **Problema:** En `JwtAuthenticationFilter.java` las excepciones se manejan con `System.err.println(...)` y `e.printStackTrace()`.
- **Riesgo:** `printStackTrace()` es bloqueante y no se integra con sistemas centralizados de logs (Datadog, ELK, CloudWatch).
- **Solución:** Usar `@Slf4j` (Lombok) e implementar logs estructurados (`log.error("...", e)`).

---

## 🏗️ 2. Arquitectura y Diseño (Architecture & Design)

### 2.1. Gestión de Multi-Tenancy (Inquilinos Múltiples)
- **Observación Positiva:** La implementación de Multi-Tenancy mediante el uso de Hibernate `@TenantId` en las entidades (`Asunto`, `Documento`, `Usuario`) y `LexFlowTenantResolver` junto a un `ThreadLocal` en `TenantContext` es el estándar oro en la actualidad para SaaS con Spring Boot 3+.
- **Punto de Mejora:** En `JwtAuthenticationFilter`, si el token es viejo y no trae `despachoId`, se le asigna el ID `0L` (tenant de seguridad). Hay que tener mucho cuidado de que ninguna entidad sea creada bajo el tenant `0L` por error y asegurar que los repositorios no permitan guardados si el ID es `0L`.

### 2.2. Validaciones en los DTOs
- **Problema:** Las peticiones (Ej. `RegistroDespachoRequest`, `LoginRequest`) no parecen estar validadas con la librería `spring-boot-starter-validation`.
- **Solución:** Usar `@NotBlank`, `@Email`, `@Size` en los DTOs, y `@Valid` en los Controladores para evitar que lógica corrupta llegue a los Servicios.

---

## 📉 3. Incongruencias y Code Quality (Mejoras Generales)

### 3.1. Spanglish (Deuda Técnica)
- **Problema:** Tal como se reconoce en algunos TODOs, hay una mezcla severa de español e inglés (Ej: `findByEmailParaLogin`, `obtenerTodos`, `AuthService`, `JwtAuthenticationFilter`).
- **Solución:** Estandarizar. Lo recomendable en proyectos profesionales es usar **inglés** en el 100% del código (Clases, Métodos, Variables, DTOs). 

### 3.2. Versión de Spring Boot en `pom.xml`
- **Problema Crítico:** El archivo `pom.xml` declara `<version>4.0.4</version>` para `spring-boot-starter-parent`. Spring Boot 4.0 **no existe** actualmente (la rama principal estable es la 3.x, ej. `3.4.0`).
- **Solución:** Corregir a la última versión estable (ej. `3.4.1`) para asegurar que las dependencias transitivas resuelvan correctamente.

### 3.3. Uso de Excepciones Genéricas
- **Problema:** En `AuthService`, se lanza `throw new RuntimeException("Error: Credenciales incorrectas.");`
- **Solución:** Crear excepciones de dominio (ej. `InvalidCredentialsException`, `ResourceNotFoundException`) y atraparlas en un `@ControllerAdvice` (`GlobalExceptionHandler`) para unificar la respuesta de errores de la API.

---

## 🚀 4. Pasos a Seguir (Actionable Steps)

1. **Rotar Secretos y Limpiar Repositorio:**
   - Rotar inmediatamente los tokens de AWS, Base de Datos y Google Drive mostrados en `application.properties`.
   - Limpiar el historial de Git (usando `BFG Repo-Cleaner` o `git filter-repo`) o al menos asegurarse de que el archivo `.properties` ahora requiera variables de entorno (`${DB_PASSWORD}`).

2. **Corregir Dependencias (POM):**
   - Modificar la versión de Spring Boot a `3.3.x` o `3.4.x` en `pom.xml`.

3. **Cerrar Brechas de Seguridad (CORS / JWT):**
   - Borrar `@CrossOrigin(origins = "*")` en todo el proyecto.
   - Modificar `AuthService` para devolver el `refreshToken` en una cookie `HttpOnly`.

4. **Mejorar Lógica de Excepciones y Logs:**
   - Añadir `@Slf4j` a las clases clave y remover los `System.out.println`.
   - Implementar un `@RestControllerAdvice` para manejar errores de forma centralizada y devolver un JSON estandarizado al frontend.

5. **Refactorización (Plan a Medio Plazo):**
   - Hacer un rename masivo de las funciones y variables de español a inglés.
