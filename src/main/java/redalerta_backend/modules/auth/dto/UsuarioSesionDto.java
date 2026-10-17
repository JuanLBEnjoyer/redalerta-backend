package redalerta_backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * DTO que encapsula la informacion esencial de identidad y permisos del usuario
 * para la inicializacion de la sesion en el cliente web o movil.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos esenciales de identidad y permisos del usuario autenticado para la sesion activa")
public class UsuarioSesionDto {

    @Schema(description = "Identificador unico del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del usuario", example = "Daniel Estiv")
    private String nombre;

    @Schema(description = "Correo electronico del usuario", example = "danielestiv@hotmail.com")
    private String email;

    @Schema(description = "Rol asignado para control de acceso y navegacion", example = "ROLE_CIUDADANO")
    private Rol rol;
}
