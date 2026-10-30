package redalerta_backend.modules.alerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que encapsula los datos para iniciar la atencion de una alerta
 * comunitaria
 * por parte de la brigada, comite comunitario o lider asignado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para registrar el inicio de atencion de una alerta por la brigada, comite comunitario o lider")
public class IniciarGestionRequestDto {

    @NotBlank(message = "La observacion inicial de atencion es obligatoria")
    @Size(max = 500, message = "La observacion inicial no puede exceder los 500 caracteres")
    @Schema(description = "Notas operativas iniciales o equipo de brigadistas en desplazamiento", example = "Brigada comunitaria en desplazamiento hacia el sector para verificar afectacion y coordinar apoyo.")
    private String observacionInicial;
}
