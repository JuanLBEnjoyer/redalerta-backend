package redalerta_backend.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponseDto {

    private String departamento;

    private String municipio;

    private String direccionResidencia;

}
