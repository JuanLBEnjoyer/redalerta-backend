package redalerta_backend.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la aprobacion de una solicitud de registro con observaciones opcionales del administrador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para la aprobacion y activacion de una solicitud de registro")
public class AprobarSolicitudRequestDto {

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    @Schema(description = "Observaciones o justificacion del administrador para la aprobacion", example = "Soportes validados y conforme a las politicas comunitarias.")
    private String observaciones;
}
