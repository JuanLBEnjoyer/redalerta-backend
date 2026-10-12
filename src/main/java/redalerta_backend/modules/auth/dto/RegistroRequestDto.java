package redalerta_backend.modules.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.dto.CaracterizacionRequestDto;
import redalerta_backend.modules.usuario.dto.DocumentoRequestDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequestDto {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "Los datos del documento son obligatorios")
    @Valid
    private DocumentoRequestDto documento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 50, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @NotNull(message = "Los datos de la encuesta de caracterización son obligatorios")
    @Valid
    private CaracterizacionRequestDto caracterizacion;
}
