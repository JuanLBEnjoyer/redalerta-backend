package redalerta_backend.modules.alerta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
 * Controlador REST enfocado unicamente en la publicacion de alertas comunitarias.
 */
@Tag(name = "Alertas", description = "Endpoints para la creacion de alertas comunitarias")
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
}