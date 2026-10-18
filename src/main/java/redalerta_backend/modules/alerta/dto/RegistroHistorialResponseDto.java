package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDateTime;

/**
 * DTO que representa un asiento de auditoria dentro del historial de la alerta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registro de trazabilidad y cambio de estado de una alerta")
public class RegistroHistorialResponseDto {

    @Schema(description = "Motivo, accion o justificacion de la transicion", example = "Alerta reportada por ciudadano")
    private String motivoOAccion;

    @Schema(description = "Fecha y hora en que se ejecuto la accion")
    private LocalDateTime fechaAccion;

    @Schema(description = "Estado resultante tras la accion", example = "PENDIENTE_REVISION")
    private EstadoAlerta estadoResultante;

    @Schema(description = "Identificador del usuario que ejecuto la accion", example = "1")
    private Long responsableId;

    @Schema(description = "Nombre del usuario responsable", example = "Juan Perez")
    private String responsableNombre;

    @Schema(description = "Rol del usuario responsable", example = "ROLE_CIUDADANO")
    private String responsableRol;
}
