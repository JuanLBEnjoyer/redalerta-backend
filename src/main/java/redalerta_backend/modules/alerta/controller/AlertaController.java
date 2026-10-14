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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.service.AlertaService;

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
     * Consulta el detalle de una alerta especifica reportada por el ciudadano autenticado.
     *
     * @param id Identificador de la alerta.
     * @param authentication Contexto de seguridad inyectado.
     * @return Detalle de la alerta encontrada.
     */
    @Operation(summary = "Consultar alerta por ID", description = "Obtiene el detalle de una alerta especifica propia del ciudadano.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerta consultada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o usuario inactivo",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (no es el autor de la alerta)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerta no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CIUDADANO')")
    public ResponseEntity<ApiResponse<AlertaResponseDto>> obtenerAlertaPorId(
            @PathVariable Long id,
            Authentication authentication) {
        AlertaResponseDto alerta = alertaService.obtenerAlertaPorId(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(alerta, "Detalle de alerta consultado exitosamente"));
    }
}