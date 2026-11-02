package redalerta_backend.modules.notificacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDateTime;

/**
 * DTO para la representacion publica de una notificacion en la bandeja de entrada y su detalle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informacion estructurada de una notificacion en la bandeja de entrada")
public class NotificacionResponseDto {

    @Schema(description = "Identificador unico de la notificacion", example = "1")
    private Long id;

    @Schema(description = "Identificador de la alerta asociada para navegacion directa", example = "15")
    private Long alertaId;

    @Schema(description = "Titulo descriptivo del evento", example = "Alerta Direccionada")
    private String titulo;

    @Schema(description = "Mensaje corto para mostrar en la lista de la bandeja", example = "Su alerta \"Hueco peligroso en vía principal...\" ha cambiado a estado DIRECCIONADA")
    private String mensaje;

    @Schema(description = "Nuevo estado de la alerta comunitaria", example = "DIRECCIONADA")
    private EstadoAlerta nuevoEstado;

    @Schema(description = "Motivo, accion, observacion tecnica o informe de resolucion del cambio", example = "Asignado a la brigada de emergencias para señalizacion")
    private String motivoAccion;

    @Schema(description = "Nombre de la persona o comite responsable de la accion", example = "Lider Comunitario Armenia")
    private String responsableNombre;

    @Schema(description = "Rol del responsable de la accion", example = "ROLE_LIDER")
    private String responsableRol;

    @Schema(description = "Indica si la notificacion ya fue leida por el usuario", example = "false")
    private Boolean leida;

    @Schema(description = "Fecha y hora en que se genero la notificacion", example = "2026-11-01T16:30:00")
    private LocalDateTime fechaCreacion;
}

