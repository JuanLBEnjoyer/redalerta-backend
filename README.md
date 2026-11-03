# RedAlerta Backend API

Servicio central del ecosistema de seguridad y autogestion comunitaria **RedAlerta**. Este backend expone una API RESTful stateless unificada, disenada para ser consumida por la aplicacion movil (Android) y el panel web administrativo.

---

## Stack Tecnologico

* **Lenguaje:** Java 25
* **Framework:** Spring Boot 4.1.1
  * Spring Web (Controladores REST y serializacion JSON)
  * Spring Security (Arquitectura stateless y autorizacion basada en roles `@PreAuthorize`)
  * Spring Data JPA (Capa de persistencia con Hibernate y `JpaSpecificationExecutor`)
  * Jakarta Bean Validation (Validacion declarativa de DTOs)
  * Spring Boot Starter Mail (Envio de correos transaccionales via SMTP / Brevo)
* **Seguridad y Criptografia:**
  * JJWT (`io.jsonwebtoken` 0.12.6) para emision, firma HMAC-SHA256 y validacion de tokens JWT de sesion y de proposito unico (`PASSWORD_RESET`)
  * `BCryptPasswordEncoder` para almacenamiento seguro de contrasenas y codigos OTP
  * `SecureRandom` para generacion criptografica de codigos de verificacion de 6 digitos
* **Almacenamiento en la Nube y Multimedia:**
  * AWS SDK for Java v2 (`software.amazon.awssdk:s3` 2.29.50) para gestion de evidencias en Amazon S3
  * Thumbnailator (`net.coobird:thumbnailator` 0.4.20) para redimensionamiento y compresion automatica de imagenes con validacion de cabeceras binarias (Magic Bytes)
* **Base de Datos:** PostgreSQL 16+
* **Documentacion API:** SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 2.8.5)
* **Testing:**
  * JUnit 5 (Jupiter Platform)
  * Mockito para pruebas unitarias aisladas (170 pruebas automatizadas)
* **Herramientas de Construccion:** Apache Maven con Maven Wrapper (`mvnw`) y soporte Docker (`Dockerfile`)

---

## Arquitectura y Modulos del Sistema

El proyecto esta construido bajo una arquitectura modular orientada al dominio (**Domain-Driven Design - DDD**) y principios **Zero Trust**:

* **Context Path Base:** `/api/v1` (ejemplo: `http://localhost:8080/api/v1/auth/login`)
* **Value Objects Inmutables:** El dominio encapsula invariantes y reglas de negocio en objetos de valor inmutables (`Documento`, `Ubicacion`, `UbicacionAlerta`, `RegistroHistorial`, `Rol`, `Genero`, `TipoDeDocumento`, `EstadoAlerta`, `CategoriaAlerta`, `PrioridadAlerta`).
* **Estandarizacion de Respuestas:**
  * **Exito (HTTP 2xx):** Envoltorio generico `ApiResponse<T>` con indicadores `success`, `message`, `data` y `timestamp`.
  * **Errores (HTTP 4xx / 5xx):** Capturados centralizadamente por `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando estructuras `ErrorResponse` consistentes sin exponer trazas internas del servidor.
* **Seguridad Stateless:** Cada solicitud protegida es interceptada por `JwtAuthenticationFilter`, que valida la firma del token portador (`Bearer`) y verifica en `CustomUserDetailsService` que la cuenta del usuario permanezca activa.

---

## Inicio Rapido

### Requisitos Previos

1. Java Development Kit (JDK) 25 instalado.
2. PostgreSQL 16+ en ejecucion local o mediante Docker.

Para levantar rapidamente una instancia de PostgreSQL 16 en Docker:
```bash
docker run -d \
  --name redalerta-postgres \
  -e POSTGRES_DB=redalerta_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

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

   > Nota de seguridad: El archivo `application.properties` se encuentra excluido del control de versiones (`.gitignore`) para evitar la filtracion de credenciales y claves secretas.

3. Configurar las variables en `application.properties` o mediante variables de entorno del sistema:

   | Variable | Descripcion | Valor por defecto (Desarrollo) |
   | :--- | :--- | :--- |
   | `DB_URL` | URL JDBC de conexion a PostgreSQL | `jdbc:postgresql://localhost:5432/redalerta_db` |
   | `DB_USERNAME` | Usuario de base de datos | `postgres` |
   | `DB_PASSWORD` | Contrasena de base de datos | `postgres` |
   | `JWT_SECRET` | Clave secreta hexadecimal (minimo 256 bits) para firmar tokens | Clave de desarrollo en plantilla |
   | `JWT_EXPIRATION_MS` | Tiempo de vida del token de sesion en milisegundos | `86400000` (24 horas) |
   | `AWS_S3_BUCKET_NAME` | Nombre del bucket en Amazon S3 para evidencias | `redalerta-evidencias` |
   | `AWS_ACCESS_KEY_ID` | Credencial de acceso IAM con permisos sobre el bucket S3 | *(Vacio en plantilla)* |
   | `AWS_SECRET_ACCESS_KEY` | Clave secreta IAM para S3 | *(Vacio en plantilla)* |
   | `AWS_REGION` | Region de AWS donde reside el bucket | `us-east-1` |
   | `SPRING_MAIL_HOST` | Servidor SMTP (ej. `smtp-relay.brevo.com` o `smtp.gmail.com`) | `smtp.gmail.com` |
   | `SPRING_MAIL_PORT` | Puerto SMTP con STARTTLS | `587` |
   | `SPRING_MAIL_USERNAME` | Usuario de autenticacion SMTP | *(Vacio: activa modo fallback en consola)* |
   | `SPRING_MAIL_PASSWORD` | Clave SMTP o API Key de envio | *(Vacio en plantilla)* |
   | `APP_MAIL_FROM` | Correo remitente verificado en el proveedor SMTP | `soporte.redalerta@gmail.com` |

### Compilacion y Ejecucion de Pruebas

Para validar la compilacion del codigo y ejecutar las pruebas unitarias:

```bash
./mvnw clean test
```

### Ejecutar la Aplicacion

```bash
./mvnw spring-boot:run
```

El servidor iniciara en el puerto `8080` bajo el contexto base `/api/v1`. Al iniciar con la base de datos vacia, el componente `TestDataSeeder` creara automaticamente usuarios semilla para todos los roles (`CIUDADANO`, `LIDER`, `ORGANIZACION` y `ADMIN`) con la contrasena de prueba `Password123!`.

---

## Documentacion Interactiva de la API (Swagger UI & OpenAPI)

Para evitar la desincronizacion entre el codigo fuente y la documentacion escrita, RedAlerta expone su especificacion viva y actualizada automaticamente mediante **SpringDoc OpenAPI 3**.

### Enlaces de Acceso

* **Consola Interactiva (Swagger UI):** [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
* **Especificacion en formato JSON:** [http://localhost:8080/api/v1/v3/api-docs](http://localhost:8080/api/v1/v3/api-docs)

### Como Explorar y Probar la API desde Swagger UI

1. **Iniciar el servidor local:**
   ```bash
   ./mvnw spring-boot:run
   ```
2. **Generar un Token de Autenticacion:**
   * Despliegue el modulo **Autenticacion** en Swagger UI.
   * Seleccione `POST /api/v1/auth/login` (puede usar una de las cuentas semilla de `TestDataSeeder`, por ejemplo `ciudadano1@redalerta.com`, `lider.armenia@redalerta.com`, `brigada.emergencias.armenia@redalerta.org` o `admin@redalerta.com` con clave `Password123!`).
   * Copie el valor del token JWT retornado en `data.token`.
3. **Autorizar las Peticiones en Swagger:**
   * En la parte superior derecha de Swagger UI, haga clic en el boton **Authorize** (candado).
   * Pegue el token en el campo **Value** del esquema `BearerAuth` y presione **Authorize**.
4. **Probar Endpoints Protegidos:**
   * Cualquier solicitud enviada desde la consola incluira automaticamente el encabezado `Authorization: Bearer <token_jwt>`.

---

## Documentacion del Proyecto

* **[Diseno Funcional y Modelo de Negocio](DISENO_FUNCIONAL_Y_MODELO_NEGOCIO.md):** Vision de producto, ecosistema de roles, modelo territorial, flujos operativos de triaje y politicas de negocio.
* **[Guia de Desarrollo y Onboarding](GUIA_DESARROLLO.md):** Estandares tecnicos de ingenieria, arquitectura limpia, flujo de trabajo GitFlow y politicas de testing.

