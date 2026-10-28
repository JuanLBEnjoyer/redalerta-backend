package redalerta_backend.modules.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoRequestDto {

    @NotBlank(message = "El número de documento es obligatorio")
    private String numero;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDeDocumento tipo;
}
