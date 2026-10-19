package redalerta_backend.modules.alerta.service;

/**
 * Clase de utilidad que centraliza la generacion de mensajes comprensibles
 * y estructurados para los cambios de estado en las alertas comunitarias.
 * Brinda textos orientados a la experiencia del ciudadano tanto para el historial
 * de auditoria como para las notificaciones del sistema.
 */
public final class MensajeAlertaHelper {

    private static final int MAX_RESUMEN_DESC = 45;

    private MensajeAlertaHelper() {
    }

    /**
     * Resume la descripcion ciudadana a una longitud adecuada agregando elipsis
     * si excede el limite definido.
     *
     * @param descripcion Descripcion original del reporte.
     * @return Descripcion limpia y sintetizada.
     */
    public static String resumirDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return "Sin descripción";
        }
        String limpia = descripcion.trim().replaceAll("\\s+", " ");
        if (limpia.length() <= MAX_RESUMEN_DESC) {
            return limpia;
        }
        return limpia.substring(0, MAX_RESUMEN_DESC).trim() + "...";
    }

    /**
     * Construye el mensaje amigable cuando una alerta es reportada por primera vez.
     *
     * @param descripcion Descripcion original del incidente.
     * @return Mensaje formal de radicacion inicial.
     */
    public static String mensajeReportada(String descripcion) {
        return String.format(
                "Su alerta \"%s\" ha sido reportada exitosamente y está pendiente de revisión comunitaria.",
                resumirDescripcion(descripcion)
        );
    }

    /**
     * Construye el mensaje amigable cuando una alerta es direccionada a una organizacion.
     *
     * @param descripcion        Descripcion original del incidente.
     * @param organizacionNombre Nombre oficial de la entidad receptora.
     * @param observacion        Observacion tecnica consignada por el lider.
     * @return Mensaje estructurado de direccionamiento.
     */
    public static String mensajeDireccionada(String descripcion, String organizacionNombre, String observacion) {
        String obs = (observacion != null && !observacion.isBlank()) ? observacion.trim() : "Sin observación técnica";
        return String.format(
                "Su alerta \"%s\" ha sido direccionada correctamente a la organización \"%s\". Observación: \"%s\"",
                resumirDescripcion(descripcion),
                organizacionNombre,
                obs
        );
    }

    /**
     * Construye el mensaje amigable cuando un lider rectifica o actualiza el triaje de una alerta direccionada.
     *
     * @param descripcion        Descripcion original del incidente.
     * @param organizacionNombre Nombre de la entidad receptora (actual o reasignada).
     * @param observacion        Nueva observacion tecnica consignada.
     * @return Mensaje estructurado de modificacion de direccionamiento.
     */
    public static String mensajeDireccionamientoModificado(String descripcion, String organizacionNombre, String observacion) {
        String obs = (observacion != null && !observacion.isBlank()) ? observacion.trim() : "Sin observación técnica";
        return String.format(
                "El direccionamiento de su alerta \"%s\" ha sido actualizado hacia la organización \"%s\". Observación: \"%s\"",
                resumirDescripcion(descripcion),
                organizacionNombre,
                obs
        );
    }

    /**
     * Construye el mensaje amigable cuando se cancela el direccionamiento de una alerta y retorna a revision.
     *
     * @param descripcion Descripcion original del incidente.
     * @param motivo      Justificacion formal de la cancelacion.
     * @return Mensaje estructurado de cancelacion.
     */
    public static String mensajeDireccionamientoCancelado(String descripcion, String motivo) {
        String mot = (motivo != null && !motivo.isBlank()) ? motivo.trim() : "Sin motivo especificado";
        return String.format(
                "El direccionamiento de su alerta \"%s\" ha sido cancelado y regresó a revisión comunitaria. Motivo: \"%s\"",
                resumirDescripcion(descripcion),
                mot
        );
    }
}
