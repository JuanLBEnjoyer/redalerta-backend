package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.CategoriaAlerta;
import redalerta_backend.modules.alerta.valueobject.PrioridadAlerta;

/**
 * DTO que encapsula los datos requeridos para que un lider comunitario clasifique
 * y direccione una alerta hacia una entidad u organizacion competente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de triaje y direccionamiento de una alerta hacia una organizacion")
public class DireccionarAlertaRequestDto {

    @NotNull(message = "La categoria de la alerta es obligatoria")
    @Schema(description = "Tipologia o clasificacion tecnica de la alerta", example = "INCENDIO")
    private CategoriaAlerta categoria;

    @NotNull(message = "La prioridad de la alerta es obligatoria")
    @Schema(description = "Nivel de urgencia asignado a la alerta", example = "ALTA")
    private PrioridadAlerta prioridad;

    @NotNull(message = "El identificador de la organizacion destino es obligatorio")
    @Schema(description = "Identificador unico de la entidad u organizacion competente", example = "15")
    private Long organizacionId;

    @NotBlank(message = "La observacion tecnica es obligatoria")
    @Size(min = 10, max = 1000, message = "La observacion tecnica debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Dictamen o notas tecnicas del lider para la intervencion de la entidad", example = "Se evidencia conflagracion activa en sector forestal con riesgo de propagacion a viviendas.")
    private String observacionTecnica;
}
