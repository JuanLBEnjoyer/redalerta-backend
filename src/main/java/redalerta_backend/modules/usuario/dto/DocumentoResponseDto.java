package redalerta_backend.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoResponseDto {

    private String numero;
    private TipoDeDocumento tipo;
}
