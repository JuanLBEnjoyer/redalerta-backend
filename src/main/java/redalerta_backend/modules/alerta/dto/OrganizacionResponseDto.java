package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que expone la informacion de contacto e identificacion de un comite o brigada
 * comunitaria elegible para la atencion y gestion de alertas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de contacto de un comite o brigada comunitaria elegible para atencion de alertas")
public class OrganizacionResponseDto {

    @Schema(description = "Identificador unico del comite o brigada comunitaria", example = "5")
    private Long id;

    @Schema(description = "Nombre oficial o denominacion del comite comunitario", example = "Brigada Comunitaria de Emergencias Armenia")
    private String nombre;

    @Schema(description = "Correo electronico de contacto del comite", example = "brigada.emergencias.armenia@redalerta.org")
    private String email;

    @Schema(description = "Telefono o linea de emergencias", example = "3101234567")
    private String telefono;

    @Schema(description = "Departamento territorial de operacion", example = "Quindío")
    private String departamento;

    @Schema(description = "Municipio territorial de operacion", example = "Armenia")
    private String municipio;
}
