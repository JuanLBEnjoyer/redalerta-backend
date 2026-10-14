package redalerta_backend.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDto {

    private Long id;
    private String nombre;
    private DocumentoResponseDto documento;
    private UbicacionResponseDto ubicacion;
    private String email;
    private String telefono;
    private Rol rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private CaracterizacionResponseDto caracterizacion;
}
