package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta retornado tras la carga y compresion exitosa de una evidencia fotografica.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la carga y compresion de una evidencia fotografica en AWS S3")
public class UploadEvidenciaResponseDto {

    @Schema(description = "URL publica o de acceso directo a la imagen en S3", example = "https://redalerta-evidencias.s3.us-east-1.amazonaws.com/alertas/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String url;

    @Schema(description = "Clave o ruta unica del objeto dentro del bucket", example = "alertas/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String key;

    @Schema(description = "Nombre original del archivo suministrado por el usuario", example = "foto_inundacion.jpg")
    private String nombreOriginal;

    @Schema(description = "Tamano original del archivo antes de la compresion en bytes", example = "8388608")
    private Long pesoOriginalBytes;

    @Schema(description = "Tamano final optimizado y almacenado en S3 en bytes", example = "358400")
    private Long pesoComprimidoBytes;

    @Schema(description = "Tipo de contenido MIME de la imagen", example = "image/jpeg")
    private String tipoMime;
}
