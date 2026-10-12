# Guía de Desarrollo y Onboarding - RedAlerta Backend

¡Bienvenido al equipo de desarrollo de **RedAlerta**! Esta guía detalla los requisitos, la configuración del entorno, la arquitectura y las reglas de colaboración para trabajar en el backend del proyecto de forma óptima y estandarizada.

##  Tabla de Contenidos

1. [Requisitos Previos y Herramientas](#1-requisitos-previos-y-herramientas-necesarias)
2. [Contexto y Arquitectura General](#2-contexto-y-arquitectura-general)
3. [Configuración del Entorno Local](#3-configuración-del-entorno-local-paso-a-paso)
4. [Estrategia de Ramas (GitFlow)](#4-estrategia-de-ramas-gitflow-y-reglas-de-oro)
5. [Convención de Commits](#5-convención-de-commits-conventional-commits)
6. [Flujo de Trabajo Diario](#6-flujo-de-trabajo-diario-paso-a-paso)

---

## 1. Requisitos Previos y Herramientas Necesarias

Antes de comenzar, asegúrate de tener instaladas las siguientes herramientas en tu equipo:

| Herramienta | Versión Recomendada | Propósito |
|---|---|---|
| **JDK (Java Development Kit)** | Java 17 (LTS) o Java 25 | Lenguaje de programación del backend. |
| **Apache Maven** | 3.8+ (o el ejecutable `./mvnw` incluido) | Gestión de dependencias y construcción del proyecto. |
| **IDE de Desarrollo** | Antigravity, IntelliJ IDEA, VS Code o STS | Entorno de desarrollo para Java y Spring Boot. |
| **Oracle Database** | 23c Free o 18c/21c XE | Motor de base de datos relacional (instalación local o Docker). |
| **Gestor de Base de Datos** | Oracle SQL Developer o DBeaver | Cliente gráfico para inspeccionar esquemas y ejecutar scripts SQL. |
| **Git** | 2.x+ | Control de versiones. |

---

## 2. Contexto y Arquitectura General

- **Framework principal:** Spring Boot 4.4.1 (Spring Web, Spring Security, Spring Data JPA, Validation).
- **Multiplataforma:** sirve a la **aplicación móvil** (Android - Java) y al **panel web de administración** mediante una API REST unificada.
- **Context path base:** todas las URLs de la API responden bajo el prefijo `/api/v1` (`http://localhost:8080/api/v1/...`).
- **Autenticación:** orientada a **JWT (stateless)**.

> **Nota:** Al iniciar la aplicación verás un login por defecto en el navegador generado por Spring Security. Esto es completamente normal en esta fase.

---

## 3. Configuración del Entorno Local Paso a Paso

### Paso 3.1: Clonar el repositorio e ir a la rama de desarrollo

```bash
# 1. Clonar el repositorio
git clone https://github.com/TU_ORGANIZACION/redalerta-backend.git
cd redalerta-backend

# 2. Posicionarse en la rama develop
git checkout develop
git pull origin develop
```

### Paso 3.2: Configurar el usuario y permisos en Oracle DB

Abre tu gestor de base de datos (SQL Developer) conectado como administrador (`system_pdb` o `system`) en el PDB `FREEPDB1` (o `XEPDB1`) y ejecuta:

```sql
-- 1. Crear el usuario para el proyecto (si no existe)
CREATE USER tu_usuario_aqui IDENTIFIED BY tu_contrasena_aqui;

-- 2. Otorgar roles básicos de conexión y creación de objetos
GRANT CONNECT, RESOURCE TO tu_usuario_aqui;

-- 3. CRÍTICO: Otorgar cuota ilimitada de almacenamiento (evita el error ORA-01950 en Hibernate)
ALTER USER tu_usuario_aqui QUOTA UNLIMITED ON USERS;
```

### Paso 3.3: Configurar las variables de entorno (`application.properties`)

El archivo `src/main/resources/application.properties` utiliza la sintaxis de reemplazo `${VARIABLE:valor_por_defecto}`. Puedes guiarte con la plantilla pública `application-example.properties` o configurar las variables de entorno directamente en tu IDE.

**Propiedades base:**

```properties
# Puerto y Context Path
server.port=${PORT:8080}
server.servlet.context-path=${SERVER_CONTEXT_PATH:/api/v1}

# Conexión a Oracle DB (¡Atención a la sintaxis del separador /!)
spring.datasource.url=${DB_URL:jdbc:oracle:thin:@localhost:1521/FREEPDB1}
spring.datasource.username=${DB_USERNAME:tu_usuario_aqui}
spring.datasource.password=${DB_PASSWORD:tu_contrasena_aqui}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Configuración JPA / Dialecto
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Seguridad (JWT)
jwt.secret=${JWT_SECRET:clave_secreta_desarrollo_al_menos_256_bits_1234567890}
jwt.expiration-ms=${JWT_EXPIRATION:86400000}
```

> **Sintaxis crítica de la URL Oracle JDBC**
>
> Para nombre de servicio / PDB (`FREEPDB1` / `XEPDB1`) **debes usar barra diagonal `/`**:
>
> ```
> jdbc:oracle:thin:@localhost:1521/FREEPDB1
> ```
>
> Usar dos puntos (`:FREEPDB1`) causará el error **ORA-12505**.

---

## 4. Estrategia de Ramas (GitFlow) y Reglas de Oro

Para mantener la integridad del código aplicamos **GitFlow estricto**.

### Ramas permanentes

| Rama | Descripción |
|---|---|
| `main` | Código oficial de entregas estables. Nadie programa ni sube cambios directamente aquí. |
| `develop` | Rama base de integración continua del equipo. Tampoco se hacen *pushes* directos. |

### Ramas temporales

| Patrón | Uso |
|---|---|
| `feature/[nombre-tarea]` | Construir nuevas funcionalidades. |
| `fix/[nombre-error]` | Corregir errores detectados. |
| `chore/[nombre-config]` | Tareas de mantenimiento o archivos de configuración. |

### Reglas estrictas de protección de ramas (Branch Protection)

1. **Pushes directos prohibidos:** intentar un `git push origin develop` o `main` será rechazado por GitHub.
2. **Pull Request (PR) obligatorio:** todos los cambios entran exclusivamente mediante un PR hacia `develop`.
3. **Aprobación de código (Peer Review):** todo PR requiere al menos **1 aprobación** de un compañero antes de fusionarse.
4. **Limpieza:** una vez fusionado el PR, la rama temporal debe eliminarse en GitHub y en local.

---

## 5. Convención de Commits (Conventional Commits)

Todos los mensajes de commit deben seguir la estructura:

```
tipo: descripción corta en minúsculas
```

| Prefijo | Uso | Ejemplo |
|---|---|---|
| `feat:` | Nueva característica o endpoint | `feat: add REST endpoint for alert creation` |
| `fix:` | Corrección de un fallo o excepción | `fix: resolve ORA-12505 database connection syntax` |
| `docs:` | Cambios en documentación o README | `docs: update setup guide with Oracle DB instructions` |
| `style:` | Formato, espacios o comentarios | `style: fix indentations in AlertaService` |
| `refactor:` | Reestructuración de código sin alterar la lógica | `refactor: optimize user query logic in repository` |
| `chore:` | Mantenimiento, `.gitignore` o dependencias | `chore: update dependencies in pom.xml` |

> **Regla de atomicidad:** haz commits separados por cada tipo de trabajo. No mezcles un `feat` con un `fix` o un `refactor` en el mismo commit.

---

##  6. Flujo de Trabajo Diario (Paso a Paso)

Cuando te asignen una tarea, sigue este ciclo exacto:

### 1. Actualizar tu rama `develop` local

```bash
git checkout develop
git pull origin develop
```

### 2. Crear tu rama de trabajo

```bash
git checkout -b feature/registro-alertas
```

### 3. Programar y realizar commits atómicos

```bash
git add .
git commit -m "feat: create alert entity and JPA repository"
```

### 4. Subir tu rama a GitHub

```bash
git push -u origin feature/registro-alertas
```

### 5. Abrir Pull Request en GitHub

1. Ingresa al repositorio en GitHub y haz clic en **Compare & pull request**.
2. **Verifica la dirección:** `base: develop` ← `compare: feature/registro-alertas`.
3. Escribe una descripción breve y solicita la revisión de un compañero.

### 6. Aprobación y fusión (Merge)

Un compañero revisa la pestaña **Files changed**, aprueba el PR y presiona **Merge pull request**. Luego se borra la rama en GitHub con el botón **Delete branch**.

### 7. Limpieza local en tu máquina

```bash
git checkout develop
git pull origin develop
git branch -d feature/registro-alertas
```