# RedAlerta Backend API

Servicio central del ecosistema de seguridad comunitaria RedAlerta. Este backend expone una API RESTful stateless unificada, diseñada para ser consumida por la aplicacion movil (Android) y el panel web administrativo.

---

## Stack Tecnologico

* **Lenguaje:** Java 25
* **Framework:** Spring Boot 4.1.1
  * Spring Web (Controladores REST y serializacion JSON)
  * Spring Security (Arquitectura stateless y autorizacion basada en roles)
  * Spring Data JPA (Capa de persistencia con Hibernate)
  * Jakarta Bean Validation (Validacion declarativa de DTOs)
* **Seguridad y Criptografia:**
  * JJWT (io.jsonwebtoken 0.12.6) para emision, firma HMAC-SHA256 y validacion de tokens JWT
  * BCryptPasswordEncoder para almacenamiento seguro de credenciales
* **Base de Datos:** Oracle Database (PDB FREEPDB1 / dialecto OracleDialect)
* **Testing:**
  * JUnit 5 (Jupiter Platform)
  * Mockito para pruebas unitarias aisladas
* **Herramientas de Construccion:** Apache Maven con Maven Wrapper (mvnw)
* **Librerias Complementarias:** Project Lombok

---

## Arquitectura del Sistema

El proyecto esta construido bajo una arquitectura modular orientada al dominio (Domain-Driven Design - DDD):

* **Context Path Base:** `/api/v1` (ejemplo: `http://localhost:8080/api/v1/auth/login`)
* **Value Objects Inmutables:** El dominio encapsula reglas de negocio en objetos de valor inmutables (`Email`, `Documento`, `Rol`, `Genero`, `TipoDeDocumento`).
* **Estandarizacion de Respuestas:**
  * **Exito (HTTP 2xx):** Envoltorio generico `ApiResponse<T>` con estado, mensaje y carga util.
  * **Errores (HTTP 4xx / 5xx):** Capturados centralizadamente por `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando estructuras `ErrorResponse` consistentes.
* **Seguridad Stateless:** Cada solicitud protegida es interceptada por `JwtAuthenticationFilter`, que extrae y valida el token portador (`Bearer`) antes de autorizar el acceso.

---

## Estructura de Paquetes

```text
src/main/java/redalerta_backend/
├── config/                          Configuracion global del framework
│   ├── SecurityConfig.java          Cadena de filtros de seguridad HTTP
│   └── security/                    Infraestructura de autenticacion JWT
│       ├── JwtService.java          Firma, parsing y validacion de tokens
│       ├── JwtAuthenticationFilter  Filtro interceptor OncePerRequestFilter
│       └── CustomUserDetailsService Adaptador de UserDetailsService
├── common/                          Componentes transversales reutilizables
│   ├── dto/                         ApiResponse<T> y ErrorResponse
│   └── exception/                   Jerarquia de excepciones y GlobalExceptionHandler
└── modules/                         Modulos de negocio por contexto delimitado
    ├── auth/                        Registro, inicio de sesion y emision de credenciales
    │   ├── controller/              AuthController
    │   ├── dto/                     LoginRequestDto, LoginResponseDto, RegistroRequestDto
    │   └── service/                 AuthService e implementacion
    ├── usuario/                     Gestion de usuarios, encuesta y caracterizacion
    │   ├── entity/                  Usuario y CaracterizacionUsuario
    │   ├── mapper/                  UsuarioMapper
    │   ├── repository/              UsuarioRepository
    │   └── valueobject/             Documento, Email, Genero, Rol, TipoDeDocumento
    └── alerta/                      Modulo de alertas comunitarias y monitoreo
```

---

## Inicio Rapido

### Requisitos Previos

1. Java Development Kit (JDK) 25 o compatible instalado.
2. Oracle Database (23c Free, 21c o 18c XE) en ejecucion local o en contenedor Docker.
3. Usuario Oracle con permisos y cuota de almacenamiento asignada en el tablespace `USERS`.

### Configuracion del Entorno

1. Clonar el repositorio y acceder al directorio raiz:
   ```bash
   git clone https://github.com/JuanLBEnjoyer/redalerta-backend.git
   cd redalerta-backend
   ```

2. Crear el archivo de configuracion local a partir de la plantilla:
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   ```

   > Nota de seguridad: El archivo `application.properties` se encuentra excluido del control de versiones (.gitignore) para evitar la filtracion de credenciales y claves secretas al repositorio publico.

3. Configurar las variables requeridas en `application.properties` o mediante variables de entorno del sistema (`JWT_SECRET`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`).

### Compilacion y Ejecucion de Pruebas

Para validar la compilacion del codigo y ejecutar la suite completa de pruebas unitarias:

```bash
sh ./mvnw clean test
```

### Ejecutar la Aplicacion

```bash
sh ./mvnw spring-boot:run
```

El servidor iniciara en el puerto `8080` bajo el contexto base `/api/v1`.

---

## Modulos y Endpoints Disponibles

### Autenticacion (`/api/v1/auth`)

| Metodo | Endpoint | Descripcion | Acceso |
|---|---|---|---|
| POST | `/api/v1/auth/registro` | Registro de usuario con encuesta de caracterizacion inicial | Publico |
| POST | `/api/v1/auth/login` | Inicio de sesion y emision de token JWT Bearer | Publico |

### Alertas (`/api/v1/alertas`)

| Metodo | Endpoint | Descripcion | Acceso |
|---|---|---|---|
| GET | `/api/v1/alertas/ping` | Verificacion de estado del modulo | Publico |
| GET | `/api/v1/alertas/prueba-error/{id}` | Endpoint de prueba para validacion de manejo de errores | Autenticado |

---

## Documentacion del Proyecto

Para consultar los estandares de desarrollo, estrategia GitFlow y politicas de contribucion, consulte la [Guia de Desarrollo y Onboarding](GUIA_DESARROLLO.md).