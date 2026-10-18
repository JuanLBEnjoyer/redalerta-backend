package redalerta_backend.modules.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO que representa un departamento con su lista oficial de municipios asociados.
 */
@Schema(description = "Catalogo de departamento con municipios asociados")
public record DepartamentoResponseDto(
        @Schema(description = "Nombre oficial del departamento", example = "Quindío")
        String departamento,

        @Schema(description = "Lista oficial de municipios pertenecientes al departamento", example = "[\"Armenia\", \"Calarcá\", \"Circasia\"]")
        List<String> municipios
) {
}
