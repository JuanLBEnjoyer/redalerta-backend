package redalerta_backend.modules.alerta.valueobject;

/**
 * Representa la tipologia o clasificacion tematica de una alerta comunitaria.
 * Permite a los lideres y entidades categorizar y direccionar la incidencia hacia
 * la organizacion idonea (ej. Bomberos, Policia, Cruz Roja, Empresas de Servicios Publicos).
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
