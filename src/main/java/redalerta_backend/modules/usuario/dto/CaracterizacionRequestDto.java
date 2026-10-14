package redalerta_backend.modules.usuario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.Genero;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaracterizacionRequestDto {

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El género es obligatorio")
    private Genero genero;

    @Builder.Default
    private Boolean enGestacionLactancia = false;

    @NotNull(message = "El número de integrantes de la familia es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 integrante en la familia")
    private Integer numeroIntegrantesFamilia;

    @Builder.Default
    private Boolean tieneMenoresDeEdad = false;

    @Builder.Default
    private Integer cantidadMenores = 0;

    @Builder.Default
    private Boolean tieneMiembroDiscapacidad = false;

    @Size(max = 200, message = "La especificación de discapacidad no puede superar los 200 caracteres")
    private String especificacionDiscapacidad;

    @Builder.Default
    private Boolean perteneceGrupoEtnico = false;

    @Size(max = 100, message = "La especificación de grupo étnico no puede superar los 100 caracteres")
    private String especificacionGrupoEtnico;
}
