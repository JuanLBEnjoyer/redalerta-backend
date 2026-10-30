package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que encapsula el informe de cierre y resolucion comunitaria de una alerta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para dar por resuelta una alerta adjuntando el informe comunitario de resolucion")
public class ResolverAlertaRequestDto {

    @NotBlank(message = "El informe de resolucion de la alerta es obligatorio")
    @Size(min = 20, max = 1500, message = "El informe de resolucion debe tener entre 20 y 1500 caracteres")
    @Schema(description = "Informe detallado de las actividades realizadas, recursos entregados y solucion implementada",
            example = "Se culminaron las labores comunitarias de dragado y limpieza del canal. Familias del sector recibieron asistencia comunitaria.")
    private String informeResolucion;
}

