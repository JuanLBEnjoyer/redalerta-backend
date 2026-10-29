package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la transferencia y validacion de la ubicacion territorial y geodesica de una alerta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos geograficos y coordenadas geodesicas GPS de la ubicacion del incidente")
public class UbicacionAlertaDto {

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede superar los 100 caracteres")
    @Schema(description = "Departamento normalizado", example = "Quindio")
    private String departamento;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede superar los 100 caracteres")
    @Schema(description = "Municipio normalizado", example = "Armenia")
    private String municipio;

    @Size(max = 255, message = "La direccion detallada no puede superar los 255 caracteres")
    @Schema(description = "Direccion detallada o indicaciones del lugar (opcional si se reporta por GPS)", example = "Carrera 14 # 12-25, frente a la plaza principal")
    private String direccionDetallada;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud minima permitida es -90.0 grados")
    @DecimalMax(value = "90.0", message = "La latitud maxima permitida es 90.0 grados")
    @Schema(description = "Latitud geodesica en grados decimales (WGS84)", example = "4.533889")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud minima permitida es -180.0 grados")
    @DecimalMax(value = "180.0", message = "La longitud maxima permitida es 180.0 grados")
    @Schema(description = "Longitud geodesica en grados decimales (WGS84)", example = "-75.681111")
    private Double longitud;

    @DecimalMin(value = "-500.0", message = "La altitud minima permitida es -500.0 metros")
    @DecimalMax(value = "9000.0", message = "La altitud maxima permitida es 9000.0 metros")
    @Schema(description = "Altitud sobre el nivel del mar en metros (opcional)", example = "1483.0")
    private Double altitud;
}
