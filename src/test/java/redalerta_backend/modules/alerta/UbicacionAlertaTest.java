package redalerta_backend.modules.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;

import static org.junit.jupiter.api.Assertions.*;

class UbicacionAlertaTest {

    @Test
    @DisplayName("Debe crear UbicacionAlerta exitosamente con coordenadas completas y canonicas")
    void crearUbicacionAlertaExitosa() {
        UbicacionAlerta ubicacion = new UbicacionAlerta(
                "Quindio",
                "Armenia",
                "Carrera 14 # 10-20",
                4.533889,
                -75.681111,
                1483.0
        );

        assertEquals("Quindio", ubicacion.departamento());
        assertEquals("Armenia", ubicacion.municipio());
        assertEquals("Carrera 14 # 10-20", ubicacion.direccionDetallada());
        assertEquals(4.533889, ubicacion.latitud());
        assertEquals(-75.681111, ubicacion.longitud());
        assertEquals(1483.0, ubicacion.altitud());
    }

    @Test
    @DisplayName("Debe asignar direccion por defecto cuando direccionDetallada es nula o vacia")
    void crearUbicacionAlertaConDireccionVaciaUsaFallbackGps() {
        UbicacionAlerta ubicacion = new UbicacionAlerta(
                "Quindio",
                "Armenia",
                "   ",
                4.533889,
                -75.681111,
                null
        );

        assertEquals("Ubicacion capturada por GPS", ubicacion.direccionDetallada());
        assertNull(ubicacion.altitud());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90.0001, 90.0001, -120.0, 100.0})
    @DisplayName("Debe lanzar BadRequestException si la latitud esta fuera del rango [-90.0, 90.0]")
    void latitudFueraDeRangoLanzaExcepcion(double latitudInvalida) {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                new UbicacionAlerta("Quindio", "Armenia", "Calle 1", latitudInvalida, -75.68, 1483.0)
        );
        assertTrue(ex.getMessage().contains("latitud"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180.0001, 180.0001, -200.0, 190.0})
    @DisplayName("Debe lanzar BadRequestException si la longitud esta fuera del rango [-180.0, 180.0]")
    void longitudFueraDeRangoLanzaExcepcion(double longitudInvalida) {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                new UbicacionAlerta("Quindio", "Armenia", "Calle 1", 4.53, longitudInvalida, 1483.0)
        );
        assertTrue(ex.getMessage().contains("longitud"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-500.1, 9000.1, 12000.0})
    @DisplayName("Debe lanzar BadRequestException si la altitud esta fuera del rango [-500.0, 9000.0]")
    void altitudFueraDeRangoLanzaExcepcion(double altitudInvalida) {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                new UbicacionAlerta("Quindio", "Armenia", "Calle 1", 4.53, -75.68, altitudInvalida)
        );
        assertTrue(ex.getMessage().contains("altitud"));
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si la latitud o longitud son nulas")
    void latitudOLongitudNulaLanzaExcepcion() {
        assertThrows(BadRequestException.class, () ->
                new UbicacionAlerta("Quindio", "Armenia", "Calle 1", null, -75.68, 1483.0)
        );
        assertThrows(BadRequestException.class, () ->
                new UbicacionAlerta("Quindio", "Armenia", "Calle 1", 4.53, null, 1483.0)
        );
    }
}

