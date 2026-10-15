# Diseno Funcional y Modelo de Negocio - RedAlerta

Documento maestro de definicion funcional del ecosistema RedAlerta. Este documento detalla la vision del producto, los roles participantes, las reglas territoriales y los flujos operativos para alinear al equipo de desarrollo en la construccion de los modulos del sistema.

---

## 1. Vision del Producto y Proposito

RedAlerta es una plataforma integral de comunicacion, triaje comunitario y respuesta temprana ante incidentes, emergencias y situaciones de vulnerabilidad o desplazamiento forzado.

### El Problema Central
En contextos comunitarios y de emergencia, existe una desconexion critica entre tres partes:
1. **El ciudadano afectado:** Enfrenta dificultades para comunicar lo que ocurre de manera estructurada y con ubicacion precisa.
2. **Los lideres sociales y comunitarios:** Reciben informacion dispersa por canales informales (WhatsApp, llamadas), sin herramientas para priorizar, clasificar o verificar.
3. **Las organizaciones y entidades de socorro:** Se saturan con reportes duplicados, informacion incompleta o incidentes fuera de su jurisdiccion territorial.

### La Solucion de RedAlerta
RedAlerta establece un **canal estructurado en tres niveles**:
- El **Ciudadano** reporta incidentes de manera simple y geolocalizada.
- El **Lider Comunitario** actua como nodo de triaje: valida la situacion, asigna severidad y canaliza el reporte hacia la entidad idonea.
- La **Organizacion** recibe en una bandeja de trabajo limpia unicamente las solicitudes formalmente direccionadas que caen dentro de su competencia territorial y funcional.

---

## 2. Ecosistema de Actores y Roles

El sistema define cuatro roles diferenciados con responsabilidades y alcances claramente delimitados:

```text
[ Ciudadano / Desplazado ] 
         │ (Publica alerta territorial)
         ▼
[ Lider Comunitario ] ──── (Triaje: clasifica, prioriza y despacha)
         │
         ▼ (Filtro por jurisdiccion: Municipal o Departamental)
[ Organizacion Gubernamental / Socorro ] ──── (Atiende y resuelve)
         ▲
         │ (Audita, valida y autoriza credenciales)
[ Administrador del Sistema ]
```

### 2.1 Ciudadano / Desplazado (`ROLE_CIUDADANO`)
- **Naturaleza:** Persona natural.
- **Proposito:** Reportar incidentes en su entorno o solicitar asistencia comunitaria.
- **Flujo de Acceso:** Registro directo e inmediato. Su cuenta se activa en el momento del registro (`activo = true`).
- **Datos que administra:**
  - Datos de cuenta: Nombre, documento de identidad, email, telefono, contrasena.
  - Ubicacion de residencia: Departamento, municipio y direccion de residencia.
  - Encuesta sociodemografica (Caracterizacion): Informacion de vulnerabilidad familiar (composicion del hogar, menores a cargo, mujeres en gestacion/lactancia, integrantes con discapacidad, pertenencia etnica).

### 2.2 Lider Comunitario (`ROLE_LIDER`)
- **Naturaleza:** Persona natural acreditada ante una comunidad (ej. miembros de Juntas de Accion Comunal - JAC, lideres barriales, gobernadores indigenas, representantes de consejos comunitarios).
- **Proposito:** Actuar como primer filtro calificado (triaje). No atiende directamente emergencias medicas o policiales, pero conoce el territorio, valida la veracidad del hecho y despacha la alerta a la institucion correspondiente.
- **Flujo de Acceso:** **Registro con admision condicionada.** El lider se postula ingresando sus datos personales, sector que representa y evidencia acreditativa. Su cuenta nace inactiva (`activo = false`) hasta que el Administrador verifica su idoneidad y aprueba su ingreso.
- **Responsabilidad Operativa:**
  - Visualizar las alertas reportadas en su municipio o sector.
  - Complementar el reporte asignando: Categoria, Prioridad, Observacion tecnica y Organizacion destinataria.

### 2.3 Organizacion (`ROLE_ORGANIZACION`)
- **Naturaleza:** **Persona juridica o institucion.** (Ej. Cuerpo de Bomberos, Defensa Civil, Cruz Roja, Policia Nacional, Alcaldias Municipales, Gobernaciones, Personerias, Comisarias de Familia, ONGs humanitarias).
- **Proposito:** Gestionar, responder y dar cierre a las solicitudes formales remitidas por los lideres comunitarios.
- **Bandeja de Trabajo:** Las organizaciones **no** monitorean un flujo abierto de quejas ciudadanas. Tienen una bandeja exclusiva de alertas direccionadas hacia ellas.
- **Flujo de Acceso:** **Registro institucional auditado.** Nace inactiva (`activo = false`) y requiere validacion documental por el Administrador.
- **Alcance Jurisdiccional:**
  - **Ambito Municipal:** Solo atiende incidentes ocurridos dentro de un municipio puntual.
  - **Ambito Departamental:** Tiene competencia sobre cualquier municipio del departamento respectivo.

### 2.4 Administrador (`ROLE_ADMIN`)
- **Naturaleza:** Personal de control y gobierno de la plataforma.
- **Responsabilidad:** Auditar solicitudes de registro de lideres y organizaciones, habilitar o suspender cuentas, supervisar la salud del sistema y parametrizar catalogos.

---

## 3. Modelo Territorial: La Columna Vertebral del Sistema

Todo en RedAlerta opera sobre coordenadas politico-administrativas estandarizadas: **Departamento**, **Municipio** y **Direccion o Punto de Referencia**.

### 3.1 Eliminacion del Texto Libre en Ubicacion
El ingreso de ubicaciones no se maneja mediante cuadros de texto libre no estructurados. Se utilizan cajas de seleccion desplegables (dropdowns) normalizadas:
- **Departamento:** Seleccion de catalogo oficial.
- **Municipio:** Seleccion condicionada al departamento previamente seleccionado.
- **Direccion detallada / Referencia:** Campo de texto descriptivo para el detalle fino (ej. "Carrera 14 # 10-20, frente a la plaza principal" o "Vereda La Linda, km 4").

### 3.2 La Ubicacion en la Cuenta de Usuario
La ubicacion territorial (`departamento`, `municipio`, `direccionResidencia`) es un atributo fundamental de la entidad `Usuario`, **independiente de la encuesta de caracterizacion**:
- Todo usuario (ciudadano, lider o delegado institucional) reside o despacha desde una ubicacion fija.
- La **Caracterizacion Sociodemografica** queda estrictamente reservada para la encuesta de vulnerabilidad familiar del ciudadano (menores de edad, lactancia, discapacidad, grupos etnicos). Una organizacion o un lider en su rol institucional no diligencian esta encuesta.

---

## 4. Politicas de Registro y Seguridad por Rol

| Caracteristica | Ciudadano | Lider Comunitario | Organizacion |
|---|---|---|---|
| Tipo de Persona | Natural | Natural | Juridica / Institucional |
| Activacion de Cuenta | Inmediata (`activo = true`) | Condicionada a revision (`activo = false`) | Condicionada a revision (`activo = false`) |
| Validacion Requerida | Verificacion de correo | Acreditacion comunitaria por Admin | Registro institucional / NIT por Admin |
| Encuesta de Vulnerabilidad | Obligatoria al registrarse | No aplica | No aplica |
| Informacion Base | Cedula, nombres, residencia, contacto | Cedula, nombres, sector/comunidad, certificado | NIT, razon social, tipo de entidad, representante |
| Acceso a Alertas | Solo las de su propia autoria | Alertas abiertas de su territorio | Alertas formalmente asignadas a su entidad |

---

## 5. Ciclo de Vida y Flujo Operativo de una Alerta

El flujo operativo se divide en cuatro fases consecutivas:

```text
[ FASE 1: Publicacion ]
  Ciudadano publica con:
  - Descripcion
  - Ubicacion (Depto / Muni / Direccion)
  - Evidencia fotografica (Opcional)
  Estado: PENDIENTE_REVISION
         │
         ▼
[ FASE 2: Triaje y Despacho ]
  Lider del municipio inspecciona la alerta:
  - Asigna Categoria (Seguridad, Desaparicion, Salud, Desastre...)
  - Asigna Prioridad (Baja, Media, Alta, Urgente)
  - Redacta Observacion de Triaje
  - Selecciona Organizacion Destino (Filtro por jurisdiccion)
  Estado: DIRECCIONADA
         │
         ▼
[ FASE 3: Atencion Institucional ]
  La Organizacion seleccionada recibe el caso en su bandeja:
  - Inicia protocolo de respuesta operativa
  Estado: EN_GESTION
         │
         ▼
[ FASE 4: Resolucion y Cierre ]
  La Organizacion documenta las acciones tomadas y concluye:
  Estado: RESUELTA o RECHAZADA (con justificacion)
```

### 5.1 Regla de Elegibilidad Territorial para el Despacho
Cuando un lider comunitario direcciona una alerta ocurrida en una ubicacion determinada (ejemplo: *Quindio / Armenia*), el catalogo de organizaciones que se le presenta para derivar la alerta cumple la siguiente regla de inclusion:
1. **Entidades con Ambito Municipal en Armenia:** Solo aquellas cuya jurisdiccion sea exactamente ese municipio (ej. Bomberos de Armenia).
2. **Entidades con Ambito Departamental en Quindio:** Aquellas con cobertura en todo el departamento (ej. Gobernacion del Quindio, Cruz Roja Seccional Quindio, Policia Departamental).
3. **Exclusion total:** Jamas se muestran entidades de otros departamentos ni entidades municipales pertenecientes a municipios distintos (ej. no apareceran los Bomberos de Pereira ni la Alcaldia de Calarca).

### 5.2 Politica de Modificacion, Cancelacion y Eliminacion de Alertas
* **Modificacion y Edicion por el Autor:** Un ciudadano puede corregir o actualizar la descripcion, ubicacion territorial o evidencias fotograficas de su reporte **unicamente** mientras la alerta permanezca en la Fase 1 (`PENDIENTE_REVISION`).
* **Eliminacion por el Autor:** De igual manera, la cancelacion o eliminacion definitiva de la alerta solo es admisible durante la Fase 1 (`PENDIENTE_REVISION`).
* **Bloqueo Operativo y Auditoria:** Tan pronto como la alerta es direccionada por un lider (Fase 2 `DIRECCIONADA`), entra en atencion institucional (Fase 3 `EN_GESTION`) o concluye (Fase 4 `RESUELTA` o `RECHAZADA`), tanto la modificacion como la eliminacion quedan estrictamente bloqueadas para salvaguardar la trazabilidad, evitar desorientacion en los equipos de rescate o respuesta y garantizar la auditoria sobre los recursos movilizados.

---

## 6. Arquitectura de Almacenamiento de Evidencias

Las alertas permiten el anexo voluntario de fotografias (ej. registro visual de una inundacion, via bloqueada o foto de persona desaparecida).

### Principio de Almacenamiento Desacoplado
- **Base de Datos Relacional:** Almacena unicamente metadatos (identificador de la alerta, URL de acceso, clave de objeto en la nube, tipo MIME y fecha de subida).
- **Almacenamiento de Objetos en la Nube (AWS S3):** Aloja los archivos binarios reales de las imagenes.
- **Beneficio Funcional:**
  - Evita saturar la base de datos de la aplicacion.
  - Asegura tiempos de respuesta ultrarrapidos en dispositivos moviles.
  - Permite servir imagenes directamente a traves de redes de distribucion sin consumir recursos de procesamiento de la API central.

---

## 7. Matriz de Endpoints y Contratos Funcionales

### Modulo de Autenticacion y Perfil
- `POST /api/v1/auth/registro`: Registro de ciudadanos junto con encuesta de vulnerabilidad inicial.
- `POST /api/v1/auth/login`: Autenticacion con credenciales y obtencion de token JWT portador.
- `GET /api/v1/usuarios/perfil`: Consulta de los datos personales, ubicacion y encuesta sociodemografica del usuario autenticado.
- `PUT /api/v1/usuarios/perfil`: Actualizacion de informacion de contacto y contrasena.

### Modulo de Alertas (Ciudadano)
- `POST /api/v1/alertas`: Publicacion de una nueva alerta con descripcion, ubicacion y evidencias opcionales (Estado inicial: `PENDIENTE_REVISION`).
- `GET /api/v1/alertas/mis-alertas`: Consulta paginada del historial de alertas generadas por el propio ciudadano.
- `GET /api/v1/alertas/{id}`: Detalle exhaustivo de una alerta puntual perteneciente al ciudadano (con proteccion estricta contra acceso no autorizado a reportes ajenos).

---

## 8. Glosario de Terminos del Negocio

- **Alerta Comunitaria:** Reporte estructurado generado por un ciudadano sobre un hecho, amenaza, emergencia o situacion que requiere atencion institucional o comunitaria.
- **Triaje:** Proceso de evaluacion y clasificacion rapida de un reporte para determinar su gravedad, categoria tematica y canalizacion prioritaria.
- **Direccionamiento:** Accion mediante la cual un lider comunitario transfiere formalmente la responsabilidad de una alerta calificada a una organizacion especifica.
- **Ambito Jurisdiccional:** Limite territorial legal en el que una entidad u organizacion tiene competencia de actuacion (Municipal o Departamental).
- **Caracterizacion Sociodemografica:** Cuestionario de perfilamiento de vulnerabilidad familiar orientado a poblacion desplazada o de especial proteccion constitucional.
- **Vetting (Admision Condicionada):** Procedimiento administrativo previo de verificacion de identidad y acreditacion de roles sensibles (Lideres y Organizaciones) para evitar suplantaciones institucionales.
