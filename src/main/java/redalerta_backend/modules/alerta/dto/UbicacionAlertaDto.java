package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la transferencia y validacion de la ubicacion territorial de una alerta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos geograficos normalizados de la ubicacion del incidente")
public class UbicacionAlertaDto {

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede superar los 100 caracteres")
    @Schema(description = "Departamento normalizado", example = "Quindio")
    private String departamento;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede superar los 100 caracteres")
    @Schema(description = "Municipio normalizado", example = "Armenia")
    private String municipio;

    @NotBlank(message = "La direccion detallada o punto de referencia es obligatoria")
    @Size(max = 255, message = "La direccion detallada no puede superar los 255 caracteres")
    @Schema(description = "Direccion detallada o indicaciones del lugar", example = "Carrera 14 # 12-25, frente a la plaza principal")
    private String direccionDetallada;
}
