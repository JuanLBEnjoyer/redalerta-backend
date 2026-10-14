package redalerta_backend.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.dto.ErrorResponse;
import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.service.AuthService;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

/**
 * Controlador REST encargado de los endpoints de autenticacion y emision de credenciales JWT.
 */
@Tag(name = "Autenticacion", description = "Endpoints para registro de nuevos usuarios e inicio de sesion con tokens JWT")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo ciudadano en la plataforma junto con su encuesta de caracterizacion sociodemografica.
     *
     * @param request Datos del usuario y encuesta de caracterizacion inicial.
     * @return Envoltorio con el usuario registrado y codigo HTTP 201 CREATED.
     */
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un usuario con rol ROLE_CIUDADANO y persiste su encuesta de caracterizacion de forma atomica.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada invalidos o documento de identidad ya registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Correo electronico ya registrado en la plataforma",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> registrar(
            @Valid @RequestBody RegistroRequestDto request) {
        UsuarioResponseDto nuevoUsuario = authService.registrar(request);
        return new ResponseEntity<>(
                ApiResponse.success(nuevoUsuario, "Usuario registrado exitosamente"),
                HttpStatus.CREATED
        );
    }

    /**
     * Autentica a un usuario mediante sus credenciales y emite un token JWT Bearer.
     *
     * @param request Credenciales del usuario (correo electronico y contrasena).
     * @return Token JWT y datos de perfil publicos del usuario autenticado.
     */
    @Operation(summary = "Iniciar sesion", description = "Valida las credenciales proporcionadas y genera un token JWT firmado criptograficamente.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inicio de sesion exitoso con emision de token JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato de credenciales invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales invalidas o cuenta inactiva",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Inicio de sesión exitoso"));
    }
}
