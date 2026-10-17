package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la autenticacion exitosa de un usuario.
 * Contiene el token JWT firmado y los datos esenciales de la sesion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta del inicio de sesion con token de autenticacion y datos minimos de sesion")
public class LoginResponseDto {

    @Schema(description = "Token de autenticacion JWT para autorizar peticiones subsecuentes", example = "eyJhbGciOiJIUzI1Ni...")
    private String token;

    @Builder.Default
    @Schema(description = "Tipo de autorizacion HTTP", example = "Bearer")
    private String tipo = "Bearer";

    @Schema(description = "Datos esenciales de sesion del usuario autenticado")
    private UsuarioSesionDto usuario;
}

