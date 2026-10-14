# Guia de Desarrollo y Onboarding - RedAlerta Backend

Guia tecnica para el equipo de desarrollo de RedAlerta. Este documento establece los estandares de ingenieria, el flujo de trabajo con GitFlow, las convenciones de confirmacion de cambios y los procedimientos de configuracion local.

---

## Tabla de Contenidos

1. [Requisitos Previos y Herramientas](#1-requisitos-previos-y-herramientas)
2. [Arquitectura y Principios de Diseno](#2-arquitectura-y-principios-de-diseno)
3. [Configuracion del Entorno Local](#3-configuracion-del-entorno-local)
4. [Estrategia de Ramas (GitFlow)](#4-estrategia-de-ramas-gitflow)
5. [Convencion de Commits (Conventional Commits)](#5-convencion-de-commits-conventional-commits)
6. [Politica de Calidad y Pruebas Unitarias](#6-politica-de-calidad-y-pruebas-unitarias)
7. [Ciclo de Desarrollo Paso a Paso](#7-ciclo-de-desarrollo-paso-a-paso)

---

## 1. Requisitos Previos y Herramientas

| Herramienta | Version Recomendada | Proposito |
|---|---|---|
| JDK (Java Development Kit) | Java 25 (o Java 21 LTS minimo) | Entorno de ejecucion y compilacion del backend |
| Apache Maven | 3.9+ (o ejecutable ./mvnw incluido) | Gestion de dependencias y empaquetado del proyecto |
| Oracle Database | 23c Free o 21c/18c XE | Motor de base de datos relacional (PDB FREEPDB1) |
| Cliente de Base de Datos | DBeaver, Oracle SQL Developer | Administracion y consulta de esquemas de datos |
| Git | 2.30+ | Sistema de control de versiones distribuido |

---

## 2. Arquitectura y Principios de Diseno

* **Documento Funcional y Reglas de Negocio:** Antes de implementar o modificar funcionalidades, revise el documento de [Diseno Funcional y Modelo de Negocio](docs/DISENO_FUNCIONAL_Y_MODELO_NEGOCIO.md) para entender el flujo de aprobaciones, roles y ciclo de vida de alertas.
* **Arquitectura Monolitica Modular:** Organizacion por contextos delimitados (DDD) dentro del paquete `modules/` (`auth`, `usuario`, `alerta`).
* **Value Objects Inmutables:** Los atributos criticos de negocio (`Email`, `Documento`, `Rol`, `Genero`, `Ubicacion`, `UbicacionAlerta`) se implementan como `record` de Java con validaciones estrictas en el constructor compacto.
* **Seguridad Stateless con JWT:** Spring Security configurado sin estado de sesion (`SessionCreationPolicy.STATELESS`). Cada solicitud autenticada debe incluir el encabezado HTTP:
  ```http
  Authorization: Bearer <token_jwt>
  ```
* **Manejo Centralizado de Excepciones:** Todas las excepciones de negocio y de validacion son interceptadas por `GlobalExceptionHandler` (`@RestControllerAdvice`), garantizando que ninguna traza de error interna se exponga al cliente.

---

## 3. Configuracion del Entorno Local

### 3.1 Clonacion y Rama Base

```bash
git clone https://github.com/JuanLBEnjoyer/redalerta-backend.git
cd redalerta-backend
git checkout develop
git pull origin develop
```

### 3.2 Configuracion del Usuario en Oracle Database

Conectese a la base de datos como administrador (`SYS` o `SYSTEM`) en la instancia PDB (`FREEPDB1`) y ejecute:

```sql
-- Creacion del usuario de aplicacion
CREATE USER tu_usuario IDENTIFIED BY tu_contrasena;

-- Asignacion de privilegios basicos
GRANT CONNECT, RESOURCE TO tu_usuario;

-- Asignacion de cuota en el tablespace USERS (Obligatorio para Hibernate)
ALTER USER tu_usuario QUOTA UNLIMITED ON USERS;
```

### 3.3 Gestion de Propiedades y Secretos (Twelve-Factor App)

El archivo `application.properties` se encuentra protegido e ignorado en `.gitignore` para prevenir fugas de secretos criptograficos o contraseñas personales.

1. Genere su archivo de configuracion local copiando la plantilla:
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   ```

2. Ajuste las variables de conexion y seguridad en su `application.properties`:
   ```properties
   # Puerto y Context Path
   server.port=8080
   server.servlet.context-path=${SERVER_CONTEXT_PATH:/api/v1}

   # Conexion a Oracle DB
   spring.datasource.url=${ORACLE_URL:jdbc:oracle:thin:@localhost:1521/FREEPDB1}
   spring.datasource.username=${ORACLE_USERNAME:tu_usuario}
   spring.datasource.password=${ORACLE_PASSWORD:tu_contrasena}
   spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

   # Dialecto Hibernate
   spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   # Clave secreta para firma JWT (minimo 256 bits)
   jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
   jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
   ```

---

## 4. Estrategia de Ramas (GitFlow)

El equipo aplica el modelo GitFlow para mantener un historial trazable y estable.

### Ramas Principales

| Rama | Nivel de Proteccion | Descripcion |
|---|---|---|
| `main` | Protegida contra push directo | Codigo validado para despliegue en produccion |
| `develop` | Protegida contra push directo | Rama de integracion para el desarrollo continuo del equipo |

### Ramas de Soporte / Trabajo

| Prefijo | Proposito | Rama Base | Rama Destino |
|---|---|---|---|
| `feature/` | Desarrollo de nuevas caracteristicas o endpoints | `develop` | `develop` |
| `fix/` | Correccion de errores en `develop` | `develop` | `develop` |
| `test/` | Implementacion o refactorizacion de pruebas unitarias | `develop` | `develop` |
| `refactor/` | Mejoras estructurales sin alterar comportamiento externo | `develop` | `develop` |
| `chore/` | Mantenimiento de dependencias, configuraciones o scripts | `develop` | `develop` |

---

## 5. Convencion de Commits (Conventional Commits)

Cada confirmacion en Git debe expresar de forma univoca el proposito del cambio, utilizando el formato:

```text
tipo(alcance): descripcion breve en minusculas y modo imperativo
```

| Tipo | Descripcion | Ejemplo |
|---|---|---|
| `feat` | Nueva funcionalidad para el usuario o API | `feat(auth): implementar endpoint de inicio de sesion` |
| `fix` | Correccion de un defecto o fallo detectado | `fix(jwt): corregir validacion de expiracion en tokens expirados` |
| `test` | Incorporacion o modificacion de pruebas unitarias/integracion | `test(auth): agregar pruebas unitarias para servicio de login` |
| `refactor` | Reestructuracion interna de codigo sin cambiar comportamiento | `refactor(usuario): migrar atributos de documento a value object` |
| `docs` | Modificaciones exclusivas en documentacion | `docs(readme): actualizar requisitos de instalacion y endpoints` |
| `chore` | Tareas de mantenimiento, dependencias o configuraciones | `chore(security): proteger archivo application.properties en gitignore` |
| `style` | Cambios de formato, espaciado o convenciones de estilo | `style(controller): ordenar importaciones y corregir identacion` |

---

## 6. Politica de Calidad y Pruebas Unitarias

Antes de enviar cualquier Pull Request, es obligatorio ejecutar la suite completa de pruebas unitarias y verificar la compilacion limpia:

```bash
sh ./mvnw clean test
```

### Reglas para Pruebas Unitarias:
1. **Aislamiento total:** Las pruebas unitarias deben ejecutarse de forma independiente, utilizando mocks (Mockito) para repositorios, servicios externos y codificadores.
2. **Cero dependencias externas en tests unitarios:** Ningun test unitario debe depender de conexiones activas a Oracle DB, servicios AWS o redes externas.
3. **Claves mock:** Para componentes criptograficos como `JwtServiceTest`, utilice claves dummy claramente identificadas para pruebas (`test_mock_secret_key...`).

---

## 7. Ciclo de Desarrollo Paso a Paso

### Paso 1: Actualizar la rama base

```bash
git checkout develop
git pull origin develop
```

### Paso 2: Crear la rama de trabajo

```bash
git checkout -b feature/nombre-de-la-tarea
```

### Paso 3: Desarrollar con confirmaciones atomicas

```bash
# Cambios de codigo de produccion
git add src/main/
git commit -m "feat(auth): implementar emision de token jwt"

# Pruebas asociadas
git add src/test/
git commit -m "test(auth): agregar pruebas unitarias para emision de token"
```

### Paso 4: Validar compilacion y pruebas locales

```bash
sh ./mvnw clean test
```

### Paso 5: Publicar la rama en GitHub

```bash
git push -u origin feature/nombre-de-la-tarea
```

### Paso 6: Crear Pull Request

1. Abrir GitHub y seleccionar la rama publicada.
2. Comprobar que la direccion sea: `base: develop` <- `compare: feature/nombre-de-la-tarea`.
3. Completar la descripcion del cambio y solicitar revision de codigo (Code Review).
4. Tras la aprobacion, fusionar mediante `Merge pull request`.

### Paso 7: Limpieza local

```bash
git checkout develop
git pull origin develop
git branch -d feature/nombre-de-la-tarea
```