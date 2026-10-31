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
import redalerta_backend.modules.auth.dto.RestablecerPasswordRequestDto;
import redalerta_backend.modules.auth.dto.SolicitarRecuperacionRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoResponseDto;
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

    /**
     * Cierra la sesion del usuario autenticado actual, limpiando el contexto de seguridad.
     *
     * @return Confirmacion de cierre de sesion con codigo HTTP 200 OK.
     */
    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión del usuario en el contexto de seguridad y notifica a la aplicación cliente para purgar sus credenciales locales.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token de autenticación ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(null, "Sesión cerrada exitosamente"));
    }

    /**
     * Solicita el restablecimiento de contrasena para una cuenta de correo electronico.
     * Genera un codigo OTP de 6 digitos y lo envia por correo electronico.
     * Retorna un mensaje generico para prevenir enumeracion de cuentas (OWASP).
     *
     * @param request DTO con el correo electronico registrado.
     * @return Mensaje generico informativo con codigo HTTP 200 OK.
     */
    @Operation(summary = "Solicitar recuperacion de contrasena", description = "Genera un codigo OTP de 6 digitos y lo envia al correo electronico si este se encuentra registrado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud procesada con exito"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato de correo electronico invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse<Void>> solicitarRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionRequestDto request) {
        authService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Si el correo electrónico coincide con una cuenta activa en el sistema, recibirá un código de recuperación de 6 dígitos válido por 10 minutos."
        ));
    }

    /**
     * Valida de forma intermedia el codigo OTP de 6 digitos ingresado por el usuario.
     * Retorna un token temporal (resetToken) firmado criptograficamente para proceder al cambio de clave.
     *
     * @param request DTO con correo electronico y codigo OTP.
     * @return Token temporal de restablecimiento y mensaje de aprobacion.
     */
    @Operation(summary = "Verificar codigo de recuperacion OTP", description = "Valida el codigo de 6 digitos enviado por correo y emite un resetToken temporal de un solo uso.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Codigo valido con emision de resetToken"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Codigo invalido, expirado o supero intentos maximos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verificar-codigo")
    public ResponseEntity<ApiResponse<VerificarCodigoResponseDto>> verificarCodigo(
            @Valid @RequestBody VerificarCodigoRequestDto request) {
        VerificarCodigoResponseDto response = authService.verificarCodigoRecuperacion(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Código verificado exitosamente"));
    }

    /**
     * Restablece la contrasena de un usuario validando el token de autorizacion temporal (resetToken).
     *
     * @param request DTO con el resetToken firmado y la nueva contrasena.
     * @return Confirmacion de cambio de contrasena con codigo HTTP 200 OK.
     */
    @Operation(summary = "Restablecer contrasena con resetToken", description = "Verifica el token temporal emitido tras validar el codigo OTP y actualiza la contrasena del usuario.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contrasena restablecida exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token invalido, expirado o formato de nueva contrasena no cumple requisitos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/restablecer-password")
    public ResponseEntity<ApiResponse<Void>> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordRequestDto request) {
        authService.restablecerPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Contraseña restablecida exitosamente. Ya puede iniciar sesión con sus nuevas credenciales."
        ));
    }
}
