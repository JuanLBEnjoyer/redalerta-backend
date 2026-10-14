package redalerta_backend.modules.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.config.security.JwtService;
import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.service.impl.AuthServiceImpl;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    @Mock
    private UsuarioRepository usuarioRepository;

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
                .nombre("Juan Perez")
                .email("juan@example.com")
                .password("$2a$10$hashedpassword")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe autenticar correctamente con credenciales válidas")
    void loginExitoso() {
        LoginRequestDto request = new LoginRequestDto("juan@example.com", "Password123*");

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123*", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtService.generateToken(usuarioMock)).thenReturn("mocked-jwt-token");
        when(usuarioMapper.toResponseDto(usuarioMock)).thenReturn(
                UsuarioResponseDto.builder()
                        .id(1L)
                        .nombre("Juan Perez")
                        .email("juan@example.com")
                        .rol(Rol.ROLE_CIUDADANO)
                        .activo(true)
                        .build());

        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("juan@example.com", response.getUsuario().getEmail());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si la contraseña no coincide")
    void loginPasswordInvalido() {
        LoginRequestDto request = new LoginRequestDto("juan@example.com", "PasswordWrong");

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("PasswordWrong", "$2a$10$hashedpassword")).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si el usuario no existe")
    void loginUsuarioNoExiste() {
        LoginRequestDto request = new LoginRequestDto("noexiste@example.com", "Password123*");

        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request));

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si la cuenta está inactiva")
    void loginUsuarioInactivo() {
        usuarioMock.setActivo(false);
        LoginRequestDto request = new LoginRequestDto("juan@example.com", "Password123*");

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Password123*", "$2a$10$hashedpassword")).thenReturn(true);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request));

        assertEquals("La cuenta de usuario se encuentra inactiva", exception.getMessage());
    }

    @Test
    @DisplayName("Debe cerrar la sesión del usuario limpiando el SecurityContext")
    void logoutExitoso() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioMock.getEmail(), null, java.util.Collections.emptyList())
        );
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        authService.logout();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
