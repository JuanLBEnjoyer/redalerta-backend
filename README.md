# RedAlerta Backend API

Servicio central del sistema de seguridad comunitaria **RedAlerta**. Este backend expone una API RESTful unificada consumida tanto por la **Aplicación Móvil (Android - Java)** como por el **Panel Web de Administración (Web)**.

---

## Stack Tecnológico

* **Lenguaje:** Java 17+ (LTS)
* **Framework:** Spring Boot 3.3.x
  * Spring Web (API RESTful)
  * Spring Security (Autenticación JWT Stateless)
  * Spring Data JPA (Persistencia / ORM)
  * Jakarta Bean Validation (Validación de DTOs)
* **Base de Datos:** Oracle Database (PDB `FREEPDB1`)
* **Herramientas & Librerías:**
  * Project Lombok
  * AWS SDK v2 (Almacenamiento en S3)
  * Firebase Admin SDK (Notificaciones Push FCM)
* **Gestión de Proyecto:** Apache Maven

---

## Arquitectura del Sistema

El proyecto está diseñado bajo la arquitectura **Monolito Modular por Dominio (Feature-per-Package)**:

* **Context Path Base:** `/api/v1` (`http://localhost:8080/api/v1/...`)
* **Estandarización de Respuestas:**
  * **Éxito (HTTP 2xx):** Envoltorio unificado `ApiResponse<T>`.
  * **Excepciones / Errores (HTTP 4xx/5xx):** Capturados globalmente mediante `@RestControllerAdvice` retornando `ErrorResponse`.

---

## Estructura del Proyecto

```text
src/main/java/com/redalerta/backend/
├── config/                         # Configuraciones de Spring (Security, CORS, AWS)
├── common/                         # Componentes reutilizables globales
│   ├── dto/                        # ApiResponse<T> y ErrorResponse
│   ├── exception/                  # Excepciones personalizadas y GlobalExceptionHandler
│   └── util/                       # Utilidades generales
└── modules/                        # Módulos de negocio (Feature-per-Package)
    ├── auth/                       # Autenticación y gestión de JWT
    ├── usuario/                    # Gestión de usuarios y roles
    ├── alerta/                     # Módulo principal de alertas comunitarias
    ├── evidencia/                  # Módulo de evidencias (AWS S3)
    └── notificacion/               # Módulo de notificaciones push (FCM)
```

---

## Inicio Rápido

### Requisitos Previos
1. JDK 17 o superior instalado.
2. Oracle DB ejecutándose localmente o en Docker.
3. Usuario Oracle configurado con cuota ilimitada (`ALTER USER jotape QUOTA UNLIMITED ON USERS;`).

### Pasos para Ejecutar
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/TU_ORGANIZACION/redalerta-backend.git
   cd redalerta-backend
   ```
2. Crear tu archivo de configuración `application.properties` basado en la plantilla:
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   ```
3. Ejecutar la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## Documentación para Desarrolladores

Para conocer el flujo de trabajo diario con **GitFlow**, convenciones de commits y detalles de la arquitectura, consulta la [Guía de Desarrollo y Onboarding](GUIA_DESARROLLO.md).