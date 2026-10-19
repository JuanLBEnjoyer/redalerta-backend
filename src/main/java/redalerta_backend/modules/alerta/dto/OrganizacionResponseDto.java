package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que expone la informacion de contacto e identificacion de una organizacion
 * u organismo de socorro elegible para la recepcion de alertas comunitarias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de contacto de una organizacion elegible para atencion de alertas")
public class OrganizacionResponseDto {

    @Schema(description = "Identificador unico de la entidad u organizacion", example = "5")
    private Long id;

    @Schema(description = "Nombre oficial o razon social de la entidad", example = "Cuerpo Oficial de Bomberos de Armenia")
    private String nombre;

    @Schema(description = "Correo electronico institucional", example = "bomberos.armenia@redalerta.gov.co")
    private String email;

    @Schema(description = "Telefono o linea de emergencias", example = "3101234567")
    private String telefono;

    @Schema(description = "Departamento territorial de operacion", example = "Quindío")
    private String departamento;

    @Schema(description = "Municipio territorial de operacion", example = "Armenia")
    private String municipio;
}
