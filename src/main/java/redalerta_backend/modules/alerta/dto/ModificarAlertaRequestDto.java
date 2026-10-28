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
 * DTO que encapsula los datos modificables de una alerta comunitaria por parte de su autor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para la modificacion de una alerta comunitaria existente")
public class ModificarAlertaRequestDto {

    @NotBlank(message = "La descripcion del incidente es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripcion debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Detalle actualizado de los hechos o situacion de alerta", example = "Se observa inundacion severa en la via principal tras fuertes lluvias impidiendo el paso vehicular.")
    private String descripcion;

    @NotNull(message = "Los datos de ubicacion son obligatorios")
    @Valid
    @Schema(description = "Ubicacion territorial actualizada del incidente")
    private UbicacionAlertaDto ubicacion;

    @Size(max = 5, message = "No se pueden adjuntar mas de 5 evidencias por alerta")
    @Schema(description = "Lista actualizada de URLs de evidencias fotograficas (maximo 5 imagenes)")
    private List<String> evidenciasUrls;

    @Schema(description = "Permite cambiar la visibilidad de la alerta a anonima o publica", example = "true")
    private Boolean esAnonimo;
}
