package redalerta_backend.modules.notificacion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.notificacion.dto.NotificacionResponseDto;
import redalerta_backend.modules.notificacion.service.NotificacionService;

/**
 * Controlador REST para la bandeja de notificaciones in-app del usuario autenticado.
 */
@Tag(name = "Notificaciones", description = "Endpoints para la consulta y administracion de la bandeja de notificaciones")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Listar notificaciones", description = "Obtiene la bandeja de notificaciones del usuario autenticado de forma paginada, con filtro opcional de solo no leidas.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bandeja de notificaciones consultada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado o token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificacionResponseDto>>> listar(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "false") Boolean soloNoLeidas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionResponseDto> pagina = notificacionService.listarNotificaciones(
                authentication.getName(), soloNoLeidas, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(pagina, "Notificaciones consultadas exitosamente"));
    }

    @Operation(summary = "Contar no leidas", description = "Retorna el total de notificaciones no leidas del usuario para mostrar en el indicador/badge de la campana.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado o token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/no-leidas/conteo")
    public ResponseEntity<ApiResponse<Long>> contarNoLeidas(Authentication authentication) {
        long conteo = notificacionService.contarNoLeidas(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(conteo, "Conteo de notificaciones no leídas"));
    }

    @Operation(summary = "Consultar detalle de notificacion", description = "Obtiene los detalles explicativos del cambio de estado y marca la notificacion como leida.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Detalle de notificacion obtenido exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado o token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionResponseDto>> obtenerDetalle(
            Authentication authentication,
            @PathVariable Long id
    ) {
        NotificacionResponseDto detalle = notificacionService.obtenerDetalle(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(detalle, "Detalle de notificación obtenido exitosamente"));
    }

    @Operation(summary = "Marcar notificacion como leida", description = "Cambia el estado de una notificacion a leida.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificacion marcada como leida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado o token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notificacion no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Void>> marcarComoLeida(
            Authentication authentication,
            @PathVariable Long id
    ) {
        notificacionService.marcarComoLeida(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Notificación marcada como leída"));
    }

    @Operation(summary = "Marcar todas como leidas", description = "Marca todas las notificaciones pendientes del usuario como leidas.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Todas las notificaciones marcadas como leidas"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado o token ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/leer-todas")
    public ResponseEntity<ApiResponse<Void>> marcarTodasComoLeidas(Authentication authentication) {
        notificacionService.marcarTodasComoLeidas(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Todas las notificaciones marcadas como leídas"));
    }
}

