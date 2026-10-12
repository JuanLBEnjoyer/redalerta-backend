package redalerta_backend.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.Genero;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaracterizacionResponseDto {

    private Long id;
    private LocalDate fechaNacimiento;
    private Integer edadCalculada;
    private Genero genero;
    private Boolean enGestacionLactancia;
    private String direccionResidencia;
    private String barrio;
    private Integer numeroIntegrantesFamilia;
    private Boolean tieneMenoresDeEdad;
    private Integer cantidadMenores;
    private Boolean tieneMiembroDiscapacidad;
    private String especificacionDiscapacidad;
    private Boolean perteneceGrupoEtnico;
    private String especificacionGrupoEtnico;
    private LocalDateTime fechaEncuesta;
}
