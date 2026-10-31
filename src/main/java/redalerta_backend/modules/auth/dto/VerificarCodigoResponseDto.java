package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta tras validar satisfactoriamente el codigo OTP.
 * Emite un token temporal de restablecimiento (resetToken) firmado criptograficamente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de verificacion de codigo con token temporal de restablecimiento")
public class VerificarCodigoResponseDto {

    @Schema(description = "Token temporal firmado criptograficamente para autorizar el cambio de contrasena", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String resetToken;

    @Schema(description = "Mensaje informativo de confirmacion", example = "Codigo verificado exitosamente")
    private String mensaje;
}

