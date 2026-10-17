package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que encapsula los datos requeridos para que un ciudadano publique una alerta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para la creacion de una nueva alerta comunitaria")
public class CrearAlertaRequestDto {

    @NotBlank(message = "La descripcion del incidente es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripcion debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Detalle de los hechos o situacion de alerta", example = "Se observa inundacion en la via principal tras fuertes lluvias impidiendo el paso.")
    private String descripcion;

    @NotNull(message = "Los datos de ubicacion son obligatorios")
    @Valid
    @Schema(description = "Ubicacion territorial del incidente")
    private UbicacionAlertaDto ubicacion;

    @Schema(description = "Lista opcional de URLs de evidencias previamente cargadas")
    private List<String> evidenciasUrls;

    @Builder.Default
    @Schema(description = "Indica si la alerta debe publicarse de forma anonima en el feed", example = "false")
    private Boolean esAnonimo = false;
}
