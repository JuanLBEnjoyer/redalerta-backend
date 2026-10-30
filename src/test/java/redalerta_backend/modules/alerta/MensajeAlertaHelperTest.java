package redalerta_backend.modules.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redalerta_backend.modules.alerta.service.MensajeAlertaHelper;

import static org.junit.jupiter.api.Assertions.*;

class MensajeAlertaHelperTest {

    @Test
    @DisplayName("Debe resumir descripcion correctamente respetando limites y elipsis")
    void resumirDescripcionCortaYLarga() {
        assertEquals("Sin descripción", MensajeAlertaHelper.resumirDescripcion(null));
        assertEquals("Sin descripción", MensajeAlertaHelper.resumirDescripcion("   "));

        String corta = "Fuga de gas en la esquina";
        assertEquals(corta, MensajeAlertaHelper.resumirDescripcion(corta));

        String larga = "Este es un reporte comunitario sumamente extenso con mas de cuarenta y cinco caracteres para probar la sintesis";
        String resultado = MensajeAlertaHelper.resumirDescripcion(larga);
        assertTrue(resultado.endsWith("..."));
        assertTrue(resultado.length() <= 48);
    }

    @Test
    @DisplayName("Debe formatear mensaje de alerta reportada")
    void mensajeReportada() {
        String msg = MensajeAlertaHelper.mensajeReportada("Hueco profundo en la via principal");
        assertEquals("Su alerta \"Hueco profundo en la via principal\" ha sido reportada exitosamente y está pendiente de revisión comunitaria.", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de alerta direccionada")
    void mensajeDireccionada() {
        String msg = MensajeAlertaHelper.mensajeDireccionada(
                "Inundacion en via de acceso",
                "Brigada Comunitaria de Emergencias Armenia",
                "Desplazamiento urgente de brigadistas"
        );
        assertEquals("Su alerta \"Inundacion en via de acceso\" ha sido direccionada correctamente al comité comunitario \"Brigada Comunitaria de Emergencias Armenia\". Observación: \"Desplazamiento urgente de brigadistas\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de direccionamiento modificado")
    void mensajeDireccionamientoModificado() {
        String msg = MensajeAlertaHelper.mensajeDireccionamientoModificado(
                "Poste de alumbrado a punto de caer",
                "Comite Barrial de Convivencia",
                "Se ajusta prioridad a critica"
        );
        assertEquals("El direccionamiento de su alerta \"Poste de alumbrado a punto de caer\" ha sido actualizado hacia el comité comunitario \"Comite Barrial de Convivencia\". Observación: \"Se ajusta prioridad a critica\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de direccionamiento cancelado")
    void mensajeDireccionamientoCancelado() {
        String msg = MensajeAlertaHelper.mensajeDireccionamientoCancelado(
                "Ruido y alteracion del orden",
                "Direccionamiento incorrecto a Brigada de Emergencias, corresponde a Comite de Convivencia"
        );
        assertEquals("El direccionamiento de su alerta \"Ruido y alteracion del orden\" ha sido cancelado y regresó a revisión comunitaria. Motivo: \"Direccionamiento incorrecto a Brigada de Emergencias, corresponde a Comite de Convivencia\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de atencion iniciada en terreno")
    void mensajeGestionIniciada() {
        String msg = MensajeAlertaHelper.mensajeGestionIniciada(
                "Derrumbe en sendero comunal",
                "Brigada de Emergencias Armenia",
                "Cuadrilla de 4 brigadistas en el punto"
        );
        assertEquals("La atención de su alerta \"Derrumbe en sendero comunal\" ha sido iniciada por \"Brigada de Emergencias Armenia\". Detalle: \"Cuadrilla de 4 brigadistas en el punto\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de alerta resuelta")
    void mensajeResuelta() {
        String msg = MensajeAlertaHelper.mensajeResuelta(
                "Arbol caido bloqueando callejon",
                "Brigada Comunitaria Armenia",
                "Se retiro el tronco con motosierras y se despejo la via comunal"
        );
        assertEquals("Su alerta \"Arbol caido bloqueando callejon\" ha sido resuelta satisfactoriamente por \"Brigada Comunitaria Armenia\". Informe de cierre: \"Se retiro el tronco con motosierras y se despejo la via comunal\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de alerta rechazada")
    void mensajeRechazada() {
        String msg = MensajeAlertaHelper.mensajeRechazada(
                "Presunta fuga en tuberia",
                "Lider Comunitario Armenia",
                "Se verifico en sitio y no existe fuga; reporte no procedente"
        );
        assertEquals("Su alerta \"Presunta fuga en tuberia\" ha sido rechazada por \"Lider Comunitario Armenia\". Motivo: \"Se verifico en sitio y no existe fuga; reporte no procedente\"", msg);
    }
}
