package redalerta_backend.modules.alerta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.DireccionarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.OrganizacionResponseDto;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;
import redalerta_backend.modules.alerta.service.AlertaService;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la publicacion y consulta de alertas por parte de ciudadanos.
 */
@Tag(name = "Alertas", description = "Endpoints para la gestion de alertas comunitarias")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    /**
     * Permite a un ciudadano autenticado publicar una nueva alerta.
     *
     * @param request Datos de descripcion, ubicacion y evidencias opcionales.
     * @param authentication Contexto de seguridad con el usuario emisor del token.
     * @return Alerta creada y codigo HTTP 201 CREATED.
     */
    @Operation(summary = "Crear nueva alerta", description = "Permite a un usuario con rol ROLE_CIUDADANO registrar una alerta comunitaria.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Alerta creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol CIUDADANO)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<AlertaResponseDto>> crearAlerta(
            @Valid @RequestBody CrearAlertaRequestDto request,
            Authentication authentication) {
        AlertaResponseDto alertaCreada = alertaService.crearAlerta(request, authentication.getName());
        return new ResponseEntity<>(
                ApiResponse.success(alertaCreada, "Alerta publicada exitosamente"),
                HttpStatus.CREATED
        );
    }

    /**
     * Permite a un ciudadano autenticado subir y optimizar una evidencia fotografica para su posterior anexo en una alerta.
     *
     * @param archivo Archivo binario de imagen (JPEG, PNG o WebP).
     * @param authentication Contexto de seguridad con el usuario emisor del token.
     * @return DTO con la URL y metadatos de la imagen almacenada en S3 y codigo HTTP 201 CREATED.
     */
    @Operation(summary = "Subir evidencia fotografica", description = "Recibe un archivo de imagen, valida su formato (JPEG, PNG o WebP), lo comprime de forma automatica y lo almacena en AWS S3.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Evidencia fotografica subida y optimizada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Archivo vacio, formato no admitido o imagen corrupta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol CIUDADANO)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Fallo en la comunicacion con el almacenamiento en la nube",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/evidencias/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<UploadEvidenciaResponseDto>> subirEvidencia(
            @Parameter(description = "Archivo binario de imagen a subir (JPEG, PNG o WebP)")
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication) {
        UploadEvidenciaResponseDto respuesta = alertaService.subirEvidencia(archivo, authentication.getName());
        return new ResponseEntity<>(
                ApiResponse.success(respuesta, "Evidencia fotografica subida y optimizada exitosamente"),
                HttpStatus.CREATED
        );
    }

    /**
     * Consulta el listado paginado de alertas creadas por el ciudadano autenticado.
     *
     * @param pageable Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad inyectado.
     * @return Pagina de alertas del usuario.
     */
    @Operation(summary = "Listar mis alertas", description = "Obtiene las alertas comunitarias reportadas por el ciudadano autenticado de forma paginada.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado de alertas recuperado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol CIUDADANO)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/mis-alertas")
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<Page<AlertaResponseDto>>> listarMisAlertas(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<AlertaResponseDto> alertas = alertaService.listarMisAlertas(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(alertas, "Alertas consultadas exitosamente"));
    }

    /**
     * Consulta el listado general paginado de alertas comunitarias con soporte de filtros.
     * El departamento es un valor fijo e inmutable determinado por la ubicacion del usuario autenticado.
     * Permite filtrar opcionalmente por municipio territorial, estado puntual y rango de fechas.
     * Por defecto retorna las alertas activas (PENDIENTE_REVISION, DIRECCIONADA, EN_GESTION).
     *
     * @param municipio Municipio territorial dentro del departamento del usuario por el cual filtrar (opcional).
     * @param estado Estado especifico por el cual filtrar (opcional).
     * @param fechaInicio Fecha inicial para el rango de creacion (opcional, formato YYYY-MM-DD).
     * @param fechaFin Fecha final para el rango de creacion (opcional, formato YYYY-MM-DD).
     * @param pageable Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad inyectado con el usuario autenticado.
     * @return Pagina de alertas comunitarias que cumplen con los filtros.
     */
    @Operation(
            summary = "Listar alertas con filtros",
            description = "Obtiene un listado paginado de alertas activas o filtradas. El departamento es un valor fijo ligado a la cuenta del usuario autenticado. Permite filtrar opcionalmente por municipio territorial, estado y rango de fechas."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado de alertas recuperado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Parametros de filtro invalidos (ej. fecha de inicio posterior a fecha de fin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('CIUDADANO', 'LIDER', 'ORGANIZACION', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertaResponseDto>>> listarAlertas(
            @Parameter(description = "Municipio territorial dentro del departamento del usuario para filtrar (opcional)")
            @RequestParam(required = false) String municipio,
            @Parameter(description = "Estado puntual de la alerta (opcional)")
            @RequestParam(required = false) EstadoAlerta estado,
            @Parameter(description = "Fecha inicial del rango de creacion (opcional, formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha final del rango de creacion (opcional, formato YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                .municipio(municipio)
                .estado(estado)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();
        Page<AlertaResponseDto> alertas = alertaService.listarAlertas(filtro, authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(alertas, "Alertas consultadas exitosamente"));
    }

    /**
     * Consulta el detalle de una alerta especifica por su identificador.
     * Permite a ciudadanos y miembros del equipo de respuesta visualizar la informacion completa
     * del incidente, ubicacion territorial, estado, evidencias y datos del autor.
     *
     * @param id Identificador unico de la alerta.
     * @param authentication Contexto de seguridad inyectado con el usuario autenticado.
     * @return Detalle de la alerta encontrada.
     */
    @Operation(summary = "Consultar detalle de alerta por ID", description = "Obtiene el detalle exhaustivo de una alerta comunitaria especifica, disponible para ciudadanos y miembros del equipo de respuesta.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta consultada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CIUDADANO', 'LIDER', 'ORGANIZACION', 'ADMIN')")
    public ResponseEntity<ApiResponse<AlertaResponseDto>> obtenerAlertaPorId(
            @PathVariable Long id,
            Authentication authentication) {
        AlertaResponseDto alerta = alertaService.obtenerAlertaPorId(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(alerta, "Detalle de alerta consultado exitosamente"));
    }

    /**
     * Permite a un ciudadano autenticado eliminar una alerta de su autoria en estado PENDIENTE_REVISION.
     *
     * @param id Identificador unico de la alerta.
     * @param authentication Contexto de seguridad inyectado.
     * @return Mensaje de confirmacion y codigo HTTP 200 OK.
     */
    @Operation(summary = "Eliminar alerta", description = "Permite a un ciudadano eliminar una alerta propia, unicamente si esta en estado PENDIENTE_REVISION.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta eliminada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La alerta no se encuentra en estado PENDIENTE_REVISION",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (no es el autor de la alerta)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<Void>> eliminarAlerta(
            @PathVariable Long id,
            Authentication authentication) {
        alertaService.eliminarAlerta(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Alerta eliminada exitosamente"));
    }

    /**
     * Permite a un ciudadano autenticado modificar una alerta de su autoria en estado PENDIENTE_REVISION.
     *
     * @param id Identificador unico de la alerta a modificar.
     * @param request Datos actualizados de la alerta (descripcion, ubicacion territorial y evidencias).
     * @param authentication Contexto de seguridad inyectado.
     * @return Alerta actualizada y codigo HTTP 200 OK.
     */
    @Operation(summary = "Modificar alerta", description = "Permite a un ciudadano modificar una alerta propia, unicamente si esta en estado PENDIENTE_REVISION.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta modificada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o estado diferente de PENDIENTE_REVISION",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (no es el autor de la alerta)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<AlertaResponseDto>> modificarAlerta(
            @PathVariable Long id,
            @Valid @RequestBody ModificarAlertaRequestDto request,
            Authentication authentication) {
        AlertaResponseDto alertaActualizada = alertaService.modificarAlerta(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(alertaActualizada, "Alerta modificada exitosamente"));
    }

    /**
     * Consulta las organizaciones de socorro o gubernamentales elegibles dentro de la jurisdiccion territorial del lider.
     *
     * @param authentication Contexto de seguridad inyectado.
     * @return Listado de organizaciones activas elegibles.
     */
    @Operation(summary = "Listar organizaciones elegibles", description = "Retorna las entidades de socorro o gubernamentales activas con competencia en la jurisdiccion del lider comunitario autenticado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado de organizaciones recuperado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol LIDER o ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/organizaciones-elegibles")
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OrganizacionResponseDto>>> listarOrganizacionesElegibles(
            Authentication authentication) {
        List<OrganizacionResponseDto> organizaciones = alertaService.listarOrganizacionesElegibles(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(organizaciones, "Organizaciones elegibles consultadas exitosamente"));
    }

    /**
     * Permite a un lider comunitario evaluar, clasificar y direccionar una alerta comunitaria a una organizacion idonea.
     *
     * @param id             Identificador de la alerta comunitaria.
     * @param request        Datos del triaje (categoria, prioridad, organizacionId, observacionTecnica).
     * @param authentication Contexto de seguridad inyectado.
     * @return Alerta direccionada y actualizada.
     */
    @Operation(summary = "Direccionar alerta", description = "Permite a un lider comunitario realizar el triaje de una alerta en PENDIENTE_REVISION, clasificarla y canalizarla hacia una entidad idonea.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta direccionada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o estado no permite direccionamiento",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (alerta fuera de la jurisdiccion del lider o rol no autorizado)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta u organizacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/direccionar")
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AlertaResponseDto>> direccionarAlerta(
            @PathVariable Long id,
            @Valid @RequestBody DireccionarAlertaRequestDto request,
            Authentication authentication) {
        AlertaResponseDto alertaDireccionada = alertaService.direccionarAlerta(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(alertaDireccionada, "Alerta direccionada exitosamente"));
    }

    /**
     * Consulta el listado paginado de alertas direccionadas por el lider comunitario autenticado.
     *
     * @param pageable       Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad inyectado.
     * @return Pagina de alertas direccionadas por el lider.
     */
    @Operation(summary = "Listar alertas direccionadas por el lider", description = "Obtiene las alertas comunitarias evaluadas y direccionadas por el lider comunitario autenticado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado de alertas direccionadas recuperado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol LIDER o ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/lider/direccionadas")
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertaResponseDto>>> listarAlertasDireccionadas(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<AlertaResponseDto> alertas = alertaService.listarAlertasDireccionadasPorLider(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(alertas, "Alertas direccionadas consultadas exitosamente"));
    }

    /**
     * Consulta la bandeja de trabajo de alertas asignadas a la organizacion de socorro o gubernamental autenticada.
     *
     * @param pageable       Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad inyectado.
     * @return Pagina de alertas en bandeja de atencion de la entidad.
     */
    @Operation(summary = "Bandeja de alertas para organizacion", description = "Obtiene las alertas comunitarias canalizadas hacia la entidad u organizacion de socorro autenticada.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bandeja de alertas recuperada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ORGANIZACION o ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/organizacion/bandeja")
    @PreAuthorize("hasAnyRole('ORGANIZACION', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<AlertaResponseDto>>> listarBandejaOrganizacion(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<AlertaResponseDto> alertas = alertaService.listarBandejaOrganizacion(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success(alertas, "Bandeja de alertas consultada exitosamente"));
    }
}