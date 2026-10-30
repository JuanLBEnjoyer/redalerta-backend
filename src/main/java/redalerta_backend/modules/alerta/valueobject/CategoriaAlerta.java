package redalerta_backend.modules.alerta.valueobject;

/**
 * Representa la tipologia o clasificacion tematica de una alerta comunitaria.
 * Permite a los lideres comunitarios categorizar y canalizar el incidente hacia
 * el comite o brigada comunitaria idonea (ej. Brigada de Emergencias, Comite de Salud, Comite de Convivencia).
 */
public enum CategoriaAlerta {
    DESASTRE_NATURAL,
    INCENDIO,
    SEGURIDAD_CIUDADANA,
    INFRAESTRUCTURA_Y_SERVICIOS,
    SALUD_Y_EMERGENCIA_MEDICA,
    AMBIENTAL,
    OTRO
}
