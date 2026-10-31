package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que encapsula los datos requeridos para fijar una nueva contrasena mediante resetToken.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para restablecer la contrasena utilizando el token de autorizacion temporal")
public class RestablecerPasswordRequestDto {

    @NotBlank(message = "El token de restablecimiento es obligatorio")
    @Schema(description = "Token temporal emitido tras validar el codigo OTP en /auth/verificar-codigo", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String resetToken;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 6, max = 50, message = "La nueva contrasena debe tener al menos 6 caracteres")
    @Schema(description = "Nueva contrasena segura", example = "NuevaPasswordSegura2026*")
    private String nuevaPassword;
}
