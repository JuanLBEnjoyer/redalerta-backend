package redalerta_backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO generico e inmutable para respuestas paginadas mediante cursor (Keyset / Seek Pagination).
 * Optimizado para feeds de scroll infinito en dispositivos moviles sin degradacion por OFFSET.
 *
 * @param <T> Tipo de los elementos contenidos en el lote.
 */
@Schema(description = "Estructura de respuesta paginada por cursor para scroll infinito")
public record CursorPageResponseDto<T>(
        @Schema(description = "Lista de elementos correspondientes al lote actual")
        List<T> content,

        @Schema(description = "Identificador del ultimo elemento entregado para usar como cursor en la siguiente peticion; null si no hay mas lotes",
                example = "42", nullable = true)
        Long nextCursor,

        @Schema(description = "Indica si existen mas registros disponibles despues de este lote", example = "true")
        boolean hasNext,

        @Schema(description = "Cantidad de elementos retornados en el lote actual", example = "10")
        int size
) {
}

