package redalerta_backend.modules.alerta.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import redalerta_backend.common.exception.BadRequestException;

import java.time.LocalDateTime;

/**
 * Value Object inmutable que representa un asiento cronologico de auditoria y trazabilidad
 * sobre los cambios de estado y acciones ejecutadas en una alerta.
 * Aplica principios de Domain-Driven Design (DDD).
 */
@Embeddable
public record RegistroHistorial(
        @Column(name = "motivo_o_accion", nullable = false, length = 500)
        String motivoOAccion,

        @Column(name = "fecha_accion", nullable = false)
        LocalDateTime fechaAccion,

        @Enumerated(EnumType.STRING)
        @Column(name = "estado_resultante", nullable = false, length = 30)
        EstadoAlerta estadoResultante,

        @Column(name = "responsable_id", nullable = false)
        Long responsableId,

        @Column(name = "responsable_nombre", nullable = false, length = 150)
        String responsableNombre,

        @Column(name = "responsable_rol", nullable = false, length = 30)
        String responsableRol
) {
    public RegistroHistorial {
        if (motivoOAccion == null || motivoOAccion.isBlank()) {
            throw new BadRequestException("El motivo o accion del historial no puede estar vacio");
        }
        if (fechaAccion == null) {
            throw new BadRequestException("La fecha del registro no puede ser nula");
        }
        if (estadoResultante == null) {
            throw new BadRequestException("El estado resultante no puede ser nulo");
        }
        if (responsableId == null) {
            throw new BadRequestException("El identificador del responsable no puede ser nulo");
        }
        if (responsableNombre == null || responsableNombre.isBlank()) {
            throw new BadRequestException("El nombre del responsable no puede estar vacio");
        }
        if (responsableRol == null || responsableRol.isBlank()) {
            throw new BadRequestException("El rol del responsable no puede estar vacio");
        }
        motivoOAccion = motivoOAccion.trim();
        responsableNombre = responsableNombre.trim();
        responsableRol = responsableRol.trim();
    }
}
