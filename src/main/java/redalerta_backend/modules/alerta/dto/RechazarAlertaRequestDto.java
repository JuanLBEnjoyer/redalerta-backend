package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que encapsula el motivo fundado para descartar o rechazar una alerta comunitaria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para descartar o rechazar una alerta comunitaria con justificacion obligatoria")
public class RechazarAlertaRequestDto {

    @NotBlank(message = "El motivo de rechazo de la alerta es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo de rechazo debe tener entre 10 y 500 caracteres")
    @Schema(description = "Justificacion clara y documentada por la cual se rechaza o descarta el reporte",
            example = "Se realizo inspeccion comunitaria en el sitio y se constató que se trataba de una falsa alarma.")
    private String motivo;
}

