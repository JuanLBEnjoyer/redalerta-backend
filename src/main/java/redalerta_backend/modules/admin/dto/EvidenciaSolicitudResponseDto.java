package redalerta_backend.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta que representa un soporte o evidencia adjunta a la solicitud.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Soporte documental adjunto a la solicitud de registro")
public class EvidenciaSolicitudResponseDto {

    @Schema(description = "Identificador unico de la evidencia", example = "1")
    private Long id;

    @Schema(description = "URL publica o de acceso al archivo en almacenamiento seguro", example = "https://s3.amazonaws.com/redalerta/soportes/cert.pdf")
    private String url;

    @Schema(description = "Nombre original o descriptivo del archivo", example = "certificado_jac.pdf")
    private String nombreArchivo;

    @Schema(description = "Fecha y hora de carga del archivo")
    private LocalDateTime fechaSubida;
}
