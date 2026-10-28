package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de cancelacion o reversion del direccionamiento de una alerta comunitaria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Peticion para cancelar o revertir el direccionamiento de una alerta hacia una entidad")
public class CancelarDireccionamientoRequestDto {

    @NotBlank(message = "El motivo de cancelacion es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo de cancelacion debe tener entre 10 y 500 caracteres")
    @Schema(
            description = "Justificacion fundamentada del motivo por el cual se revoca el direccionamiento",
            example = "Se direcciono inicialmente a Bomberos por error de digitacion, corresponde atencion de Policia Nacional"
    )
    private String motivo;
}
