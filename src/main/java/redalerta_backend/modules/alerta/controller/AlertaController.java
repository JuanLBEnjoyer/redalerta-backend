package redalerta_backend.modules.alerta.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;
import redalerta_backend.modules.alerta.service.AlertaService;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDate;

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
     * Permite filtrar opcionalmente por departamento, municipio, estado puntual y rango de fechas (fechaInicio y fechaFin).
     * Por defecto retorna las alertas activas (PENDIENTE_REVISION, DIRECCIONADA, EN_GESTION).
     *
     * @param departamento Nombre del departamento territorial a filtrar (opcional).
     * @param municipio Nombre del municipio territorial a filtrar (opcional).
     * @param estado Estado especifico por el cual filtrar (opcional).
     * @param fechaInicio Fecha inicial para el rango de creacion (opcional, formato YYYY-MM-DD).
     * @param fechaFin Fecha final para el rango de creacion (opcional, formato YYYY-MM-DD).
     * @param pageable Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad inyectado con el usuario autenticado.
     * @return Pagina de alertas comunitarias que cumplen con los filtros.
     */
    @Operation(summary = "Listar alertas con filtros", description = "Obtiene un listado paginado de alertas activas o filtradas por ubicacion (departamento, municipio), estado y rango de fechas.")
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
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) EstadoAlerta estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                .departamento(departamento)
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
}