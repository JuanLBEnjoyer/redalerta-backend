package redalerta_backend.modules.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.usuario.dto.ActualizarPerfilRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.service.UsuarioService;

/**
 * Controlador REST encargado de la gestion y consulta de perfil de usuario.
 * Requiere autenticacion Bearer JWT para todas sus operaciones.
 */
@Tag(name = "Usuarios", description = "Endpoints para consulta y actualizacion del perfil del usuario")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Consulta el perfil completo y encuesta de caracterizacion del usuario autenticado.
     *
     * @param authentication Contexto de seguridad inyectado con los datos del token JWT.
     * @return Envoltorio con la informacion del perfil del usuario.
     */
    @Operation(summary = "Consultar perfil", description = "Obtiene los datos del usuario autenticado a partir del token JWT portador.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil consultado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o token invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> obtenerPerfil(
            Authentication authentication) {
        UsuarioResponseDto perfil = usuarioService.obtenerPerfil(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(perfil, "Perfil consultado exitosamente"));
    }

    /**
     * Actualiza los datos personales permitidos (nombre, email, telefono, ubicacion de residencia y contrasena) del usuario autenticado.
     *
     * @param authentication Contexto de seguridad inyectado con los datos del token JWT.
     * @param request        Cuerpo con los campos a modificar.
     * @return Envoltorio con la informacion actualizada del perfil.
     */
    @Operation(summary = "Actualizar perfil", description = "Modifica los datos permitidos del usuario autenticado, incluyendo su ubicacion de residencia. Para cambiar la contrasena se debe suministrar la clave actual.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o contrasena actual incorrecta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o cuenta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El nuevo correo electronico ya esta registrado por otro usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilRequestDto request) {
        UsuarioResponseDto perfilActualizado = usuarioService.actualizarPerfil(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(perfilActualizado, "Perfil actualizado exitosamente"));
    }
}
