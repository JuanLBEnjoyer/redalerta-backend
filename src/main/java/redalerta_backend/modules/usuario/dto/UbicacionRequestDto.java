package redalerta_backend.modules.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la captura y respuesta de la ubicacion territorial del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos territoriales y direccion de residencia del usuario")
public class UbicacionRequestDto {

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede superar los 100 caracteres")
    @Schema(description = "Departamento de residencia", example = "Quindio")
    private String departamento;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede superar los 100 caracteres")
    @Schema(description = "Municipio de residencia", example = "Armenia")
    private String municipio;

    @NotBlank(message = "La direccion de residencia es obligatoria")
    @Size(max = 150, message = "La direccion no puede superar los 150 caracteres")
    @Schema(description = "Direccion detallada de residencia", example = "Calle 10 # 20-30")
    private String direccionResidencia;
}
