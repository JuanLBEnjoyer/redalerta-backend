package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDate;

/**
 * DTO que encapsula los criterios de filtrado para la consulta de alertas comunitarias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Criterios de filtrado para el listado de alertas")
public class FiltroAlertaDto {

    @Schema(description = "Filtro por departamento territorial del incidente", example = "Quindio")
    private String departamento;

    @Schema(description = "Filtro por municipio territorial del incidente", example = "Armenia")
    private String municipio;

    @Schema(description = "Estado puntual de la alerta. Si no se especifica, se consultan alertas activas", example = "PENDIENTE_REVISION")
    private EstadoAlerta estado;

    @Schema(description = "Fecha inicial del rango de creacion (inclusive)", example = "2026-10-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha final del rango de creacion (inclusive)", example = "2026-10-16")
    private LocalDate fechaFin;
}
