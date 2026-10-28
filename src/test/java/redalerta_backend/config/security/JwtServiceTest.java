package redalerta_backend.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Rol;

import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "test_mock_secret_key_only_for_unit_tests_1234567890_min_32_chars");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    @DisplayName("Debe generar un token JWT válido y extraer el username, rol, id y ubicacion")
    void testGenerarYExtraerToken() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .ubicacion(new Ubicacion("Quindio", "Armenia", "Calle 10 # 20-30"))
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        String token = jwtService.generateToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("juan@example.com", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("ROLE_CIUDADANO", jwtService.extractRol(token));
        assertEquals("Quindío", jwtService.extractDepartamento(token));
        assertEquals("Armenia", jwtService.extractMunicipio(token));
        assertTrue(jwtService.isTokenValid(token));
    }
}
