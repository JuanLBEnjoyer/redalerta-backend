package redalerta_backend.modules.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de actualizacion de datos de perfil del usuario.
 * El documento de identidad, rol y estado no son modificables por seguridad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para la actualizacion del perfil del usuario")
public class ActualizarPerfilRequestDto {

    @NotBlank(message = "El nombre no puede estar vacio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Maria Gomez")
    private String nombre;

    @NotBlank(message = "El correo electronico no puede estar vacio")
    @Email(message = "El formato del correo electronico no es valido")
    @Schema(description = "Correo electronico de contacto", example = "maria.gomez@example.com")
    private String email;

    @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
    @Schema(description = "Numero telefonico de contacto", example = "3109876543")
    private String telefono;

    @Schema(description = "Contrasena actual (requerida solo si se va a modificar la contrasena)", example = "PasswordActual123*")
    private String passwordActual;

    @Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
    @Schema(description = "Nueva contrasena (opcional, dejar vacio si no se desea cambiar)", example = "PasswordNuevo456*")
    private String passwordNuevo;
}
