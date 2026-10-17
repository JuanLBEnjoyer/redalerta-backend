package redalerta_backend.modules.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.admin.dto.SolicitudRegistroResponseDto;
import redalerta_backend.modules.admin.service.AdminSolicitudService;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * Controlador REST para la gestion administrativa y revision de solicitudes de registro (Vetting).
 */
@Tag(name = "Administracion - Solicitudes", description = "Endpoints para la auditoria, revision y gestion de postulaciones de registro")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/admin/solicitudes-registro")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSolicitudController {

    private final AdminSolicitudService adminSolicitudService;

    /**
     * Consulta el listado paginado de solicitudes de registro en la bandeja del administrador.
     * Por defecto retorna las postulaciones pendientes de revision, permitiendo filtrar por rol y estado.
     *
     * @param estado Estado de la solicitud a filtrar (opcional, por defecto PENDIENTE).
     * @param rol Rol del solicitante por el cual filtrar (opcional: ROLE_LIDER o ROLE_ORGANIZACION).
     * @param pageable Parametros de paginacion y ordenamiento.
     * @param authentication Contexto de seguridad con las credenciales del administrador autenticado.
     * @return Pagina de solicitudes de registro encontradas.
     */
    @Operation(
            summary = "Consultar bandeja de postulaciones",
            description = "Obtiene la bandeja paginada de solicitudes de registro de lideres y organizaciones. Por defecto muestra las pendientes de verificacion."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bandeja de solicitudes recuperada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Filtro de rol invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o cuenta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SolicitudRegistroResponseDto>>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) Rol rol,
            @PageableDefault(size = 10, sort = "fechaPostulacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<SolicitudRegistroResponseDto> solicitudes = adminSolicitudService.listarSolicitudes(
                estado,
                rol,
                authentication.getName(),
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(solicitudes, "Bandeja de solicitudes consultada exitosamente"));
    }

    /**
     * Consulta el detalle exhaustivo y evidencias de una solicitud de registro por su identificador.
     *
     * @param id Identificador unico de la solicitud de registro.
     * @param authentication Contexto de seguridad con las credenciales del administrador autenticado.
     * @return Detalle de la postulacion y sus soportes documentales.
     */
    @Operation(
            summary = "Consultar detalle y evidencias de una postulacion",
            description = "Obtiene el detalle exhaustivo de una solicitud de registro institucional o comunitaria, incluyendo sus soportes probatorios y datos del postulante."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detalle de la solicitud recuperado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o cuenta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud de registro no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SolicitudRegistroResponseDto>> obtenerSolicitudPorId(
            @PathVariable Long id,
            Authentication authentication) {
        SolicitudRegistroResponseDto solicitud = adminSolicitudService.obtenerSolicitudPorId(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(solicitud, "Detalle de la solicitud consultado exitosamente"));
    }
}
