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
                "Incendio en lote baldio",
                "Bomberos Armenia",
                "Desplazamiento urgente de cuadrilla"
        );
        assertEquals("Su alerta \"Incendio en lote baldio\" ha sido direccionada correctamente a la organización \"Bomberos Armenia\". Observación: \"Desplazamiento urgente de cuadrilla\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de direccionamiento modificado")
    void mensajeDireccionamientoModificado() {
        String msg = MensajeAlertaHelper.mensajeDireccionamientoModificado(
                "Poste de energia a punto de caer",
                "EPA Armenia",
                "Se ajusta prioridad a critica"
        );
        assertEquals("El direccionamiento de su alerta \"Poste de energia a punto de caer\" ha sido actualizado hacia la organización \"EPA Armenia\". Observación: \"Se ajusta prioridad a critica\"", msg);
    }

    @Test
    @DisplayName("Debe formatear mensaje de direccionamiento cancelado")
    void mensajeDireccionamientoCancelado() {
        String msg = MensajeAlertaHelper.mensajeDireccionamientoCancelado(
                "Robo a mano armada",
                "Direccionamiento incorrecto a Bomberos, corresponde a Policia"
        );
        assertEquals("El direccionamiento de su alerta \"Robo a mano armada\" ha sido cancelado y regresó a revisión comunitaria. Motivo: \"Direccionamiento incorrecto a Bomberos, corresponde a Policia\"", msg);
    }
}
