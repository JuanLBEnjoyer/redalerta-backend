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
* **Base de Datos:** PostgreSQL 16+ (dialecto PostgreSQLDialect)
* **Testing:**
  * JUnit 5 (Jupiter Platform)
  * Mockito para pruebas unitarias aisladas
* **Herramientas de Construccion:** Apache Maven con Maven Wrapper (mvnw)
* **Librerias Complementarias:** Project Lombok

---

## Arquitectura del Sistema

El proyecto esta construido bajo una arquitectura modular orientada al dominio (Domain-Driven Design - DDD):

* **Context Path Base:** `/api/v1` (ejemplo: `http://localhost:8080/api/v1/auth/login`)
* **Value Objects Inmutables:** El dominio encapsula reglas de negocio en objetos de valor inmutables (`Documento`, `Ubicacion`, `UbicacionAlerta`, `Rol`, `Genero`, `TipoDeDocumento`).
* **Estandarizacion de Respuestas:**
  * **Exito (HTTP 2xx):** Envoltorio generico `ApiResponse<T>` con estado, mensaje y carga util.
  * **Errores (HTTP 4xx / 5xx):** Capturados centralizadamente por `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando estructuras `ErrorResponse` consistentes.
* **Seguridad Stateless:** Cada solicitud protegida es interceptada por `JwtAuthenticationFilter`, que extrae y valida el token portador (`Bearer`) antes de autorizar el acceso.

---

## Inicio Rapido

### Requisitos Previos

1. Java Development Kit (JDK) 25 o compatible instalado.
2. PostgreSQL 16+ en ejecucion local o en contenedor Docker.
3. Base de datos `redalerta_db` creada para el proyecto.

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

3. Configurar las variables requeridas en `application.properties` o mediante variables de entorno del sistema (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

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

## Documentacion Interactiva de la API (Swagger UI & OpenAPI)

Para evitar la desincronizacion entre el codigo fuente y la documentacion escrita, RedAlerta expone su especificacion viva y actualizada automaticamente mediante **SpringDoc OpenAPI 3**.

### Enlaces de Acceso

* **Consola Interactiva (Swagger UI):** [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
* **Especificacion en formato JSON:** [http://localhost:8080/api/v1/v3/api-docs](http://localhost:8080/api/v1/v3/api-docs)

### Como Explorar y Probar la API desde Swagger UI

Swagger UI permite inspeccionar todos los endpoints agrupados por modulos (`Auth`, `Usuarios`, `Alertas`), sus esquemas DTO con validaciones, y ejecutar peticiones directamente:

1. **Iniciar el servidor local:**
   ```bash
   sh ./mvnw spring-boot:run
   ```
2. **Generar un Token de Autenticacion:**
   - Despliegue el modulo **Auth** en Swagger UI.
   - Seleccione el endpoint `POST /api/v1/auth/login` (o cree un usuario nuevo en `POST /api/v1/auth/registro`).
   - Haga clic en **Try it out**, ingrese las credenciales y presione **Execute**.
   - Copie el valor del token JWT retornado en el campo `data.token`.
3. **Autorizar las Peticiones en Swagger:**
   - En la parte superior derecha de Swagger UI, haga clic en el boton **Authorize** (candado).
   - En la ventana emergente, pegue el token en el campo **Value** del esquema `BearerAuth`.
   - Haga clic en **Authorize** y luego en **Close**.
4. **Probar Endpoints Protegidos:**
   - A partir de este momento, cualquier solicitud enviada desde la consola (como consultar perfiles, publicar alertas, paginar listados o eliminar alertas) incluira automaticamente el encabezado HTTP:
     ```http
     Authorization: Bearer <token_jwt>
     ```
   - Podra visualizar en tiempo real los codigos de respuesta HTTP (200, 201, 400, 401, 403, 404) y la estructura de los payloads.

---

## Documentacion del Proyecto

* **[Diseno Funcional y Modelo de Negocio](DISENO_FUNCIONAL_Y_MODELO_NEGOCIO.md):** Vision de producto, ecosistema de roles, modelo territorial, flujos operativos de triaje y politicas de negocio.
* **[Guia de Desarrollo y Onboarding](GUIA_DESARROLLO.md):** Estandares tecnicos de ingenieria, arquitectura limpia, flujo de trabajo GitFlow y politicas de testing.
