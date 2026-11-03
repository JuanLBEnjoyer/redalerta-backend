package redalerta_backend.modules.ubicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO inmutable que representa un departamento de Colombia y su lista oficial de municipios
 * cargados desde el recurso de datos DIVIPOLA.
 */
@Schema(description = "Estructura de un departamento de Colombia con sus respectivos municipios")
public record DepartamentoMunicipiosDto(
        @Schema(description = "Identificador secuencial del departamento", example = "25")
        Integer id,

        @Schema(description = "Nombre oficial del departamento", example = "Quindío")
        String departamento,

        @Schema(description = "Lista ordenada de municipios pertenecientes al departamento",
                example = "[\"Armenia\", \"Buenavista\", \"Calarcá\", \"Circasia\"]")
        List<String> municipios
) {
}

