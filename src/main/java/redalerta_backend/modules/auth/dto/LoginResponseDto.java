package redalerta_backend.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;

    @Builder.Default
    private String tipo = "Bearer";

    private UsuarioResponseDto usuario;
}
