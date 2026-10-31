package redalerta_backend.modules.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.mail.EmailService;
import redalerta_backend.config.security.JwtService;
import redalerta_backend.modules.auth.dto.RestablecerPasswordRequestDto;
import redalerta_backend.modules.auth.dto.SolicitarRecuperacionRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoResponseDto;
import redalerta_backend.modules.auth.entity.CodigoRecuperacion;
import redalerta_backend.modules.auth.repository.CodigoRecuperacionRepository;
import redalerta_backend.modules.auth.service.impl.AuthServiceImpl;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.CaracterizacionUsuarioRepository;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRecuperacionPasswordTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CaracterizacionUsuarioRepository caracterizacionRepository;

    @Mock
    private CodigoRecuperacionRepository codigoRecuperacionRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Maria Rodriguez")
                .email("maria@example.com")
                .password("$2a$10$hashedoldpassword")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();
    }

    // =========================================================================
    // FASE 1: SOLICITAR RECUPERACION (POST /auth/recuperar-password)
    // =========================================================================

    @Test
    @DisplayName("solicitarRecuperacion: Usuario existe genera codigo OTP de 6 digitos y envia correo")
    void solicitarRecuperacion_UsuarioExiste_GeneraCodigoYEnviaCorreo() {
        SolicitarRecuperacionRequestDto request = SolicitarRecuperacionRequestDto.builder()
                .email("maria@example.com")
                .build();

        CodigoRecuperacion codigoPrevio = CodigoRecuperacion.builder()
                .id(99L)
                .usuario(usuarioMock)
                .codigoHash("$2a$10$hashprevio")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findByUsuarioAndUsadoFalse(usuarioMock))
                .thenReturn(List.of(codigoPrevio));
        when(passwordEncoder.encode(any(String.class))).thenReturn("$2a$10$encodednewcode");

        authService.solicitarRecuperacionPassword(request);

        assertTrue(codigoPrevio.getUsado());
        verify(codigoRecuperacionRepository).saveAll(List.of(codigoPrevio));

        ArgumentCaptor<CodigoRecuperacion> codigoCaptor = ArgumentCaptor.forClass(CodigoRecuperacion.class);
        verify(codigoRecuperacionRepository).save(codigoCaptor.capture());
        CodigoRecuperacion codigoGuardado = codigoCaptor.getValue();

        assertEquals(usuarioMock, codigoGuardado.getUsuario());
        assertEquals("$2a$10$encodednewcode", codigoGuardado.getCodigoHash());
        assertFalse(codigoGuardado.getUsado());
        assertTrue(codigoGuardado.getFechaExpiracion().isAfter(LocalDateTime.now()));

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarCodigoRecuperacion(eq("maria@example.com"), eq("Maria Rodriguez"), otpCaptor.capture());
        String otpEnviado = otpCaptor.getValue();
        assertNotNull(otpEnviado);
        assertEquals(6, otpEnviado.length());
        assertTrue(otpEnviado.matches("^[0-9]{6}$"));
    }

    @Test
    @DisplayName("solicitarRecuperacion: Usuario inexistente no lanza excepcion por anti-enumeracion (OWASP)")
    void solicitarRecuperacion_UsuarioInexistente_NoLanzaExcepcion() {
        SolicitarRecuperacionRequestDto request = SolicitarRecuperacionRequestDto.builder()
                .email("desconocido@example.com")
                .build();

        when(usuarioRepository.findByEmail("desconocido@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.solicitarRecuperacionPassword(request));

        verify(codigoRecuperacionRepository, never()).save(any());
        verify(codigoRecuperacionRepository, never()).saveAll(any());
        verify(emailService, never()).enviarCodigoRecuperacion(any(), any(), any());
    }

    @Test
    @DisplayName("solicitarRecuperacion: Usuario inactivo no genera codigo ni envia correo")
    void solicitarRecuperacion_UsuarioInactivo_NoGeneraCodigo() {
        usuarioMock.setActivo(false);
        SolicitarRecuperacionRequestDto request = SolicitarRecuperacionRequestDto.builder()
                .email("maria@example.com")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));

        assertDoesNotThrow(() -> authService.solicitarRecuperacionPassword(request));

        verify(codigoRecuperacionRepository, never()).save(any());
        verify(emailService, never()).enviarCodigoRecuperacion(any(), any(), any());
    }

    // =========================================================================
    // FASE 2: VERIFICAR CODIGO OTP (POST /auth/verificar-codigo)
    // =========================================================================

    @Test
    @DisplayName("verificarCodigo: Flujo exitoso marca codigo como usado y emite resetToken")
    void verificarCodigo_Exitoso() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("654321")
                .build();

        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(20L)
                .usuario(usuarioMock)
                .codigoHash("$2a$10$hashCorrecto")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(7))
                .usado(false)
                .intentosFallidos(0)
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuarioMock))
                .thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches("654321", "$2a$10$hashCorrecto")).thenReturn(true);
        when(jwtService.generatePasswordResetToken("maria@example.com")).thenReturn("reset.jwt.token");

        VerificarCodigoResponseDto respuesta = authService.verificarCodigoRecuperacion(request);

        assertNotNull(respuesta);
        assertEquals("reset.jwt.token", respuesta.getResetToken());
        assertTrue(codigoRecuperacion.getUsado());
        verify(codigoRecuperacionRepository).save(codigoRecuperacion);
    }

    @Test
    @DisplayName("verificarCodigo: Usuario no existe lanza BadRequestException generica")
    void verificarCodigo_UsuarioNoExiste_LanzaBadRequestException() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("fantasma@example.com")
                .codigo("123456")
                .build();

        when(usuarioRepository.findByEmail("fantasma@example.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertEquals("El codigo de recuperacion es invalido o ha expirado", ex.getMessage());
    }

    @Test
    @DisplayName("verificarCodigo: Cuenta de usuario inactiva lanza BadRequestException")
    void verificarCodigo_UsuarioInactivo_LanzaBadRequestException() {
        usuarioMock.setActivo(false);
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("123456")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertEquals("La cuenta de usuario se encuentra inactiva", ex.getMessage());
    }

    @Test
    @DisplayName("verificarCodigo: Sin codigo activo pendiente lanza BadRequestException")
    void verificarCodigo_SinCodigoActivo_LanzaBadRequestException() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("123456")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuarioMock))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertEquals("El codigo de recuperacion es invalido o ha expirado", ex.getMessage());
    }

    @Test
    @DisplayName("verificarCodigo: Codigo expirado lanza BadRequestException y lo marca como usado")
    void verificarCodigo_CodigoExpirado_LanzaBadRequestExceptionYMarcaUsado() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("123456")
                .build();

        CodigoRecuperacion codigoExpirado = CodigoRecuperacion.builder()
                .id(10L)
                .usuario(usuarioMock)
                .codigoHash("$2a$10$encodedCode")
                .fechaExpiracion(LocalDateTime.now().minusMinutes(2))
                .usado(false)
                .fechaCreacion(LocalDateTime.now().minusMinutes(12))
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuarioMock))
                .thenReturn(Optional.of(codigoExpirado));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertTrue(ex.getMessage().contains("ha expirado"));
        assertTrue(codigoExpirado.getUsado());
        verify(codigoRecuperacionRepository).save(codigoExpirado);
    }

    @Test
    @DisplayName("verificarCodigo: Codigo incorrecto incrementa intentos fallidos")
    void verificarCodigo_CodigoIncorrecto_IncrementaIntentos() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("000000")
                .build();

        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(20L)
                .usuario(usuarioMock)
                .codigoHash("$2a$10$hashCorrecto")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(7))
                .usado(false)
                .intentosFallidos(1)
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuarioMock))
                .thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches("000000", "$2a$10$hashCorrecto")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertTrue(ex.getMessage().contains("Intentos restantes: 1"));
        assertEquals(2, codigoRecuperacion.getIntentosFallidos());
        assertFalse(codigoRecuperacion.getUsado());
        verify(codigoRecuperacionRepository).save(codigoRecuperacion);
    }

    @Test
    @DisplayName("verificarCodigo: Superar 3 intentos fallidos quema definitivamente el codigo OTP")
    void verificarCodigo_SuperaMaximoIntentos_QuemaCodigo() {
        VerificarCodigoRequestDto request = VerificarCodigoRequestDto.builder()
                .email("maria@example.com")
                .codigo("000000")
                .build();

        CodigoRecuperacion codigoRecuperacion = CodigoRecuperacion.builder()
                .id(20L)
                .usuario(usuarioMock)
                .codigoHash("$2a$10$hashCorrecto")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(7))
                .usado(false)
                .intentosFallidos(2)
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(codigoRecuperacionRepository.findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuarioMock))
                .thenReturn(Optional.of(codigoRecuperacion));
        when(passwordEncoder.matches("000000", "$2a$10$hashCorrecto")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.verificarCodigoRecuperacion(request));
        assertTrue(ex.getMessage().contains("Ha superado el numero maximo de intentos"));
        assertEquals(3, codigoRecuperacion.getIntentosFallidos());
        assertTrue(codigoRecuperacion.getUsado());
        verify(codigoRecuperacionRepository).save(codigoRecuperacion);
    }

    // =========================================================================
    // FASE 3: RESTABLECER CONTRASENA CON RESET-TOKEN (POST /auth/restablecer-password)
    // =========================================================================

    @Test
    @DisplayName("restablecerPassword: Con resetToken valido actualiza contrasena exitosamente")
    void restablecerPassword_ConResetTokenValido_ActualizaPassword() {
        RestablecerPasswordRequestDto request = RestablecerPasswordRequestDto.builder()
                .resetToken("token.valido.jwt")
                .nuevaPassword("SuperPassword2026*")
                .build();

        when(jwtService.isPasswordResetTokenValid("token.valido.jwt")).thenReturn(true);
        when(jwtService.extractUsername("token.valido.jwt")).thenReturn("maria@example.com");
        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.encode("SuperPassword2026*")).thenReturn("$2a$10$nuevaSuperHashedPassword");

        authService.restablecerPassword(request);

        assertEquals("$2a$10$nuevaSuperHashedPassword", usuarioMock.getPassword());
        verify(usuarioRepository).save(usuarioMock);
    }

    @Test
    @DisplayName("restablecerPassword: Con resetToken invalido o expirado lanza BadRequestException")
    void restablecerPassword_ConResetTokenInvalido_LanzaBadRequestException() {
        RestablecerPasswordRequestDto request = RestablecerPasswordRequestDto.builder()
                .resetToken("token.adulterado.jwt")
                .nuevaPassword("SuperPassword2026*")
                .build();

        when(jwtService.isPasswordResetTokenValid("token.adulterado.jwt")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.restablecerPassword(request));
        assertTrue(ex.getMessage().contains("token de restablecimiento es invalido o ha expirado"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("restablecerPassword: Si usuario asociado al token no existe lanza BadRequestException")
    void restablecerPassword_UsuarioNoExiste_LanzaBadRequestException() {
        RestablecerPasswordRequestDto request = RestablecerPasswordRequestDto.builder()
                .resetToken("token.valido.jwt")
                .nuevaPassword("SuperPassword2026*")
                .build();

        when(jwtService.isPasswordResetTokenValid("token.valido.jwt")).thenReturn(true);
        when(jwtService.extractUsername("token.valido.jwt")).thenReturn("fantasma@example.com");
        when(usuarioRepository.findByEmail("fantasma@example.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.restablecerPassword(request));
        assertTrue(ex.getMessage().contains("no fue encontrado"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("restablecerPassword: Si cuenta del usuario asociado al token esta inactiva lanza BadRequestException")
    void restablecerPassword_UsuarioInactivo_LanzaBadRequestException() {
        usuarioMock.setActivo(false);
        RestablecerPasswordRequestDto request = RestablecerPasswordRequestDto.builder()
                .resetToken("token.valido.jwt")
                .nuevaPassword("SuperPassword2026*")
                .build();

        when(jwtService.isPasswordResetTokenValid("token.valido.jwt")).thenReturn(true);
        when(jwtService.extractUsername("token.valido.jwt")).thenReturn("maria@example.com");
        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.restablecerPassword(request));
        assertEquals("La cuenta de usuario se encuentra inactiva", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}
