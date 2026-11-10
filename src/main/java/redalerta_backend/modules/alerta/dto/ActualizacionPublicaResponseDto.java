package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDateTime;

/**
 * DTO que representa una actualizacion o comunicado publico de seguimiento
 * sobre el avance de una alerta comunitaria para la ciudadania.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Actualizacion publica sobre el estado y avance de atencion de una alerta")
public class ActualizacionPublicaResponseDto {

    @Schema(description = "Identificador de la alerta asociada", example = "10")
    private Long alertaId;

    @Schema(description = "Titulo sintetico o estado del avance", example = "Atención en terreno iniciada")
    private String titulo;

    @Schema(description = "Detalle publico, motivo o informe del progreso", example = "La brigada comunitaria se encuentra en la zona realizando labores de mitigación")
    private String mensaje;

    @Schema(description = "Estado resultante tras la actualizacion", example = "EN_GESTION")
    private EstadoAlerta estado;

    @Schema(description = "Nombre de la entidad, brigada o lider que emite la actualizacion", example = "Brigada Comunitaria de Emergencias")
    private String entidadResponsable;

    @Schema(description = "Rol del responsable que emite la actualizacion", example = "ROLE_ORGANIZACION")
    private String rolResponsable;

    @Schema(description = "Fecha y hora en que se registro la actualizacion")
    private LocalDateTime fechaActualizacion;
}

