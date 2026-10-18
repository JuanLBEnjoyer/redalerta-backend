package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con la informacion de la alerta creada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de la alerta creada")
public class AlertaResponseDto {

    @Schema(description = "Identificador unico de la alerta", example = "1")
    private Long id;

    @Schema(description = "Descripcion del incidente")
    private String descripcion;

    @Schema(description = "Ubicacion territorial del incidente")
    private UbicacionAlertaDto ubicacion;

    @Schema(description = "Identificador del ciudadano autor", example = "5")
    private Long autorId;

    @Schema(description = "Nombre del ciudadano autor", example = "Juan Perez")
    private String autorNombre;

    @Schema(description = "Estado inicial de la alerta", example = "PENDIENTE_REVISION")
    private EstadoAlerta estado;

    @Schema(description = "Categoria tematica de la alerta", example = "DESASTRE_NATURAL")
    private redalerta_backend.modules.alerta.valueobject.CategoriaAlerta categoria;

    @Schema(description = "Nivel de prioridad o urgencia asignado a la alerta", example = "ALTA")
    private redalerta_backend.modules.alerta.valueobject.PrioridadAlerta prioridad;

    @Schema(description = "Evidencias fotograficas adjuntas")
    private List<EvidenciaResponseDto> evidencias;

    @Schema(description = "Historial cronologico de cambios de estado y acciones ejecutadas")
    private List<RegistroHistorialResponseDto> historial;

    @Schema(description = "Indica si la alerta fue publicada de forma anonima", example = "false")
    private Boolean esAnonimo;

    @Schema(description = "Total de evidencias fotograficas adjuntas a la alerta", example = "4")
    private Integer totalEvidencias;

    @Schema(description = "Fecha y hora de creacion de la alerta")
    private LocalDateTime fechaCreacion;
}
