# Lexflow Backend - Sistema de Gestión Jurídica

Este documento describe las funcionalidades, estructura y tecnologías del backend de **Lexflow**, un sistema diseñado para la gestión y seguimiento de asuntos legales, tareas, documentos y clientes.

## 🛠 Tecnologías Utilizadas

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.x (Spring Boot Starter Parent 4.0.4)
- **Base de Datos:** PostgreSQL
- **Seguridad:** Spring Security con autenticación basada en JWT (JSON Web Token)
- **Gestión de Dependencias:** Maven
- **Documentación:** OpenAPI / Swagger (Springdoc)
- **Persistencia:** Spring Data JPA con Hibernate
- **Utilidades:** Lombok para reducción de código repetitivo

## 🏛 Estructura del Proyecto

El sistema sigue una arquitectura de capas estándar de Spring Boot:

- **Controller:** Puntos de entrada de la API (REST Endpoints).
- **Service:** Lógica de negocio del sistema.
- **Repository:** Interfases de acceso a datos utilizando JPA.
- **Model:** Entidades que representan las tablas de la base de datos.
- **DTO (Data Transfer Objects):** Objetos para el intercambio de datos entre capas y la API.
- **Security:** Configuración de seguridad, filtros JWT y servicios de usuario.

## 🚀 Funcionalidades Principales

### 1. Gestión de Seguridad y Autenticación
- **Registro y Login:** Autenticación de usuarios mediante correo electrónico y contraseña (hasheada con BCrypt).
- **JWT (Access Token):** Generación de tokens para sesiones seguras.
- **Refresh Token:** Implementación de tokens de refresco persistidos en base de datos para mantener la sesión sin re-autenticar constantemente.
- **Roles de Usuario:** Control de acceso basado en roles (ej. `ADMIN`, `ABOGADO`).

### 2. Gestión de Asuntos (Expedientes)
- **CRUD Completo:** Creación, consulta, actualización y eliminación de asuntos jurídicos.
- **Tipificación:** Clasificación de los asuntos por tipos (ej. Civil, Penal, Laboral).
- **Asignación de Abogados:** Relación de muchos a muchos entre asuntos y usuarios para definir qué abogados trabajan en qué casos.

### 3. Gestión de Clientes
- Registro y mantenimiento de la información de contacto y datos legales de los clientes.

### 4. Seguimiento Procesal
- **Etapas Procesales:** Definición de las fases por las que atraviesa un asunto (ej. Demanda, Pruebas, Sentencia).
- **Movimientos Procesales:** Registro cronológico de las actuaciones y eventos dentro de cada etapa del proceso legal.

### 5. Gestión de Tareas
- Creación de tareas vinculadas a asuntos específicos.
- Asignación de responsables.
- Seguimiento de estados: `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`, `CANCELADA`.

### 6. Gestión de Documentos
- **Carga de Archivos:** Soporte para subir documentos (PDF, imágenes, etc.) asociados a un asunto y una etapa procesal.
- **Integración con Google Drive:** Servicio para el almacenamiento externo de archivos (actualmente en modo *mock* para desarrollo).
- **Estados del Documento:** Control de estados (ej. `BORRADOR`, `FINAL`, `FIRMADO`) e historial de cambios de estado.

## 📡 API Endpoints Principales

| Prefijo | Funcionalidad |
| :--- | :--- |
| `/api/auth` | Login y gestión de tokens. |
| `/api/usuarios` | Gestión de usuarios del sistema. |
| `/api/asuntos` | Administración de expedientes legales. |
| `/api/clientes` | Administración de la base de datos de clientes. |
| `/api/tareas` | Gestión de tareas y asignaciones. |
| `/api/documentos` | Subida y consulta de archivos. |
| `/api/etapas` | Configuración de etapas procesales. |
| `/api/movimientos` | Registro de actividad procesal. |

---
*Documento generado automáticamente tras el análisis del repositorio Lexflow-backend.*
