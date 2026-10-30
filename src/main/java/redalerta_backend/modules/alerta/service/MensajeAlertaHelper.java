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
     * Construye el mensaje amigable cuando una alerta es direccionada a un comite o brigada comunitaria.
     *
     * @param descripcion        Descripcion original del incidente.
     * @param organizacionNombre Nombre del comite o brigada comunitaria receptora.
     * @param observacion        Observacion tecnica consignada por el lider.
     * @return Mensaje estructurado de direccionamiento.
     */
    public static String mensajeDireccionada(String descripcion, String organizacionNombre, String observacion) {
        String obs = (observacion != null && !observacion.isBlank()) ? observacion.trim() : "Sin observación técnica";
        return String.format(
                "Su alerta \"%s\" ha sido direccionada correctamente al comité comunitario \"%s\". Observación: \"%s\"",
                resumirDescripcion(descripcion),
                organizacionNombre,
                obs
        );
    }

    /**
     * Construye el mensaje amigable cuando un lider rectifica o actualiza el direccionamiento hacia un comite o brigada.
     *
     * @param descripcion        Descripcion original del incidente.
     * @param organizacionNombre Nombre del comite o brigada comunitaria receptora (actual o reasignada).
     * @param observacion        Nueva observacion tecnica consignada.
     * @return Mensaje estructurado de modificacion de direccionamiento.
     */
    public static String mensajeDireccionamientoModificado(String descripcion, String organizacionNombre, String observacion) {
        String obs = (observacion != null && !observacion.isBlank()) ? observacion.trim() : "Sin observación técnica";
        return String.format(
                "El direccionamiento de su alerta \"%s\" ha sido actualizado hacia el comité comunitario \"%s\". Observación: \"%s\"",
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

    /**
     * Construye el mensaje cuando la brigada o lider inicia formalmente la atencion en terreno.
     *
     * @param descripcion        Descripcion original del incidente.
     * @param responsableNombre  Nombre del brigadista, comite o lider que asume la atencion.
     * @param observacionInicial Nota inicial u operativa.
     * @return Mensaje estructurado de inicio de atencion.
     */
    public static String mensajeGestionIniciada(String descripcion, String responsableNombre, String observacionInicial) {
        String obs = (observacionInicial != null && !observacionInicial.isBlank()) ? observacionInicial.trim() : "Atención comunitaria iniciada en terreno";
        return String.format(
                "La atención de su alerta \"%s\" ha sido iniciada por \"%s\". Detalle: \"%s\"",
                resumirDescripcion(descripcion),
                responsableNombre,
                obs
        );
    }

    /**
     * Construye el mensaje cuando una alerta comunitaria ha sido resuelta.
     *
     * @param descripcion       Descripcion original del incidente.
     * @param responsableNombre Nombre del comite o lider que resolvio la situacion.
     * @param informeResolucion Informe comunitario de las acciones de solucion.
     * @return Mensaje estructurado de cierre exitoso.
     */
    public static String mensajeResuelta(String descripcion, String responsableNombre, String informeResolucion) {
        return String.format(
                "Su alerta \"%s\" ha sido resuelta satisfactoriamente por \"%s\". Informe de cierre: \"%s\"",
                resumirDescripcion(descripcion),
                responsableNombre,
                informeResolucion != null ? informeResolucion.trim() : "Caso atendido y resuelto por la comunidad"
        );
    }

    /**
     * Construye el mensaje cuando una alerta comunitaria ha sido rechazada o descartada.
     *
     * @param descripcion       Descripcion original del incidente.
     * @param responsableNombre Nombre de quien desestimo el reporte.
     * @param motivo            Justificacion formal del descarte.
     * @return Mensaje estructurado de rechazo.
     */
    public static String mensajeRechazada(String descripcion, String responsableNombre, String motivo) {
        return String.format(
                "Su alerta \"%s\" ha sido rechazada por \"%s\". Motivo: \"%s\"",
                resumirDescripcion(descripcion),
                responsableNombre,
                motivo != null ? motivo.trim() : "Reporte no procedente"
        );
    }
}
