package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de verificacion intermedia del codigo OTP de recuperacion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de verificacion de codigo OTP para recuperacion de contrasena")
public class VerificarCodigoRequestDto {

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo electronico no es valido")
    @Schema(description = "Correo electronico de la cuenta", example = "juan.perez@example.com")
    private String email;

    @NotBlank(message = "El codigo de verificacion es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El codigo de verificacion debe contener exactamente 6 digitos numericos")
    @Schema(description = "Codigo OTP de 6 digitos recibido por correo", example = "482915")
    private String codigo;
}

