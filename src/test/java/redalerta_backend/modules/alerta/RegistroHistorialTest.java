package redalerta_backend.modules.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.RegistroHistorial;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RegistroHistorialTest {

    @Test
    @DisplayName("Debe crear un RegistroHistorial valido recortando espacios")
    void crearRegistroHistorialValido() {
        LocalDateTime ahora = LocalDateTime.now();
        RegistroHistorial registro = new RegistroHistorial(
                "  Alerta reportada por ciudadano  ",
                ahora,
                EstadoAlerta.PENDIENTE_REVISION,
                1L,
                "  Carlos Gomez  ",
                "  ROLE_CIUDADANO  "
        );

        assertNotNull(registro);
        assertEquals("Alerta reportada por ciudadano", registro.motivoOAccion());
        assertEquals(ahora, registro.fechaAccion());
        assertEquals(EstadoAlerta.PENDIENTE_REVISION, registro.estadoResultante());
        assertEquals(1L, registro.responsableId());
        assertEquals("Carlos Gomez", registro.responsableNombre());
        assertEquals("ROLE_CIUDADANO", registro.responsableRol());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si motivoOAccion es nulo o vacio")
    void lanzaExcepcionSiMotivoInvalido() {
        LocalDateTime ahora = LocalDateTime.now();
        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                null, ahora, EstadoAlerta.PENDIENTE_REVISION, 1L, "Admin", "ROLE_ADMIN"
        ));

        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "   ", ahora, EstadoAlerta.PENDIENTE_REVISION, 1L, "Admin", "ROLE_ADMIN"
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si fechaAccion o estadoResultante son nulos")
    void lanzaExcepcionSiFechaOEstadoNulos() {
        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "Accion", null, EstadoAlerta.PENDIENTE_REVISION, 1L, "Admin", "ROLE_ADMIN"
        ));

        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "Accion", LocalDateTime.now(), null, 1L, "Admin", "ROLE_ADMIN"
        ));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si datos del responsable son nulos o vacios")
    void lanzaExcepcionSiResponsableInvalido() {
        LocalDateTime ahora = LocalDateTime.now();
        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "Accion", ahora, EstadoAlerta.PENDIENTE_REVISION, null, "Admin", "ROLE_ADMIN"
        ));

        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "Accion", ahora, EstadoAlerta.PENDIENTE_REVISION, 1L, "   ", "ROLE_ADMIN"
        ));

        assertThrows(BadRequestException.class, () -> new RegistroHistorial(
                "Accion", ahora, EstadoAlerta.PENDIENTE_REVISION, 1L, "Admin", "   "
        ));
    }

    @Test
    @DisplayName("Debe registrar paso en el historial de la entidad Alerta y actualizar su estado")
    void alertaRegistraPasoHistorialCorrectamente() {
        Usuario lider = Usuario.builder()
                .id(5L)
                .nombre("Maria Lider")
                .rol(Rol.ROLE_LIDER)
                .build();

        Alerta alerta = Alerta.builder()
                .id(10L)
                .descripcion("Incidente")
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .build();

        assertEquals(0, alerta.getHistorial().size());
        assertEquals(EstadoAlerta.PENDIENTE_REVISION, alerta.getEstado());

        alerta.registrarPasoHistorial("Verificacion en campo exitosa", EstadoAlerta.EN_GESTION, lider);

        assertEquals(1, alerta.getHistorial().size());
        assertEquals(EstadoAlerta.EN_GESTION, alerta.getEstado());

        RegistroHistorial entry = alerta.getHistorial().get(0);
        assertEquals("Verificacion en campo exitosa", entry.motivoOAccion());
        assertEquals(EstadoAlerta.EN_GESTION, entry.estadoResultante());
        assertEquals(5L, entry.responsableId());
        assertEquals("Maria Lider", entry.responsableNombre());
        assertEquals("ROLE_LIDER", entry.responsableRol());
    }
}
