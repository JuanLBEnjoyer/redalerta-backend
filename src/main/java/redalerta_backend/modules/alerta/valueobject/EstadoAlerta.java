package redalerta_backend.modules.alerta.valueobject;

/**
 * Representa los diferentes estados en el ciclo de vida de una alerta comunitaria.
 */
public enum EstadoAlerta {
    PENDIENTE_REVISION,
    EN_GESTION,
    DIRECCIONADA,
    RESUELTA,
    RECHAZADA
}
