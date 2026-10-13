package redalerta_backend.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Email;
import redalerta_backend.modules.usuario.valueobject.Rol;

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
    @DisplayName("Debe generar un token JWT válido y extraer el username")
    void testGenerarYExtraerToken() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email(new Email("juan@example.com"))
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        String token = jwtService.generateToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("juan@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token));
    }
}
