package redalerta_backend.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el rechazo de una solicitud de registro con el motivo o justificacion administrativa obligatoria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para rechazar una solicitud de registro")
public class RechazarSolicitudRequestDto {

    @NotBlank(message = "El motivo de rechazo es obligatorio")
    @Size(max = 1000, message = "El motivo de rechazo no puede superar los 1000 caracteres")
    @Schema(
            description = "Motivo o justificacion administrativa del rechazo de la postulacion",
            example = "El documento soporte adjunto no corresponde a la personeria juridica de la entidad."
    )
    private String motivo;
}
