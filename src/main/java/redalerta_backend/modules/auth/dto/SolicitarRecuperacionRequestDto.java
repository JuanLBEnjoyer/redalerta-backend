package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que encapsula la solicitud inicial para recuperar contrasena mediante envio de codigo OTP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para el envio de un codigo OTP de recuperacion al correo electronico")
public class SolicitarRecuperacionRequestDto {

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "El formato del correo electronico no es valido")
    @Schema(description = "Correo electronico de la cuenta a recuperar", example = "juan.perez@example.com")
    private String email;
}

