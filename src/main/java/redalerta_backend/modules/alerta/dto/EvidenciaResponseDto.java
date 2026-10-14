package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que expone informacion publica de una evidencia multimedia vinculada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadatos de la evidencia fotografica asociada")
public class EvidenciaResponseDto {

    @Schema(description = "Identificador de la evidencia", example = "1")
    private Long id;

    @Schema(description = "URL de acceso a la imagen", example = "https://bucket-redalerta.s3.amazonaws.com/alertas/foto.jpg")
    private String url;

    @Schema(description = "Tipo MIME del contenido", example = "image/jpeg")
    private String tipoMime;

    @Schema(description = "Fecha de subida del archivo")
    private LocalDateTime fechaSubida;
}
