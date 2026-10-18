package redalerta_backend.modules.usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.EmailAlreadyExistException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.modules.usuario.dto.ActualizarPerfilRequestDto;
import redalerta_backend.modules.usuario.dto.UbicacionRequestDto;
import redalerta_backend.modules.usuario.dto.UbicacionResponseDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.service.impl.UsuarioServiceImpl;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Maria Gomez")
                .documento(new Documento("1094857291", TipoDeDocumento.CEDULA))
                .email("maria@example.com")
                .telefono("3101112233")
                .password("$2a$10$hashedActual")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe obtener el perfil del usuario autenticado exitosamente")
    void obtenerPerfilExitoso() {
        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(usuarioMapper.toResponseDto(usuarioMock)).thenReturn(
                UsuarioResponseDto.builder()
                        .id(1L)
                        .nombre("Maria Gomez")
                        .email("maria@example.com")
                        .rol(Rol.ROLE_CIUDADANO)
                        .activo(true)
                        .build()
        );

        UsuarioResponseDto perfil = usuarioService.obtenerPerfil("maria@example.com");

        assertNotNull(perfil);
        assertEquals("Maria Gomez", perfil.getNombre());
        assertEquals("maria@example.com", perfil.getEmail());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el usuario a consultar no existe")
    void obtenerPerfilUsuarioNoExiste() {
        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.obtenerPerfil("noexiste@example.com"));
    }

    @Test
    @DisplayName("Debe actualizar perfil exitosamente sin cambio de contrasena")
    void actualizarPerfilExitosoSinPassword() {
        ActualizarPerfilRequestDto request = ActualizarPerfilRequestDto.builder()
                .nombre("Maria Gomez Actualizado")
                .email("maria@example.com")
                .telefono("3209998877")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(usuarioMapper.toResponseDto(usuarioMock)).thenReturn(
                UsuarioResponseDto.builder()
                        .id(1L)
                        .nombre("Maria Gomez Actualizado")
                        .email("maria@example.com")
                        .telefono("3209998877")
                        .rol(Rol.ROLE_CIUDADANO)
                        .activo(true)
                        .build()
        );

        UsuarioResponseDto respuesta = usuarioService.actualizarPerfil("maria@example.com", request);

        assertNotNull(respuesta);
        assertEquals("Maria Gomez Actualizado", respuesta.getNombre());
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("Debe actualizar perfil y contrasena cuando la clave actual coincide")
    void actualizarPerfilConCambioPasswordExitoso() {
        ActualizarPerfilRequestDto request = ActualizarPerfilRequestDto.builder()
                .nombre("Maria Gomez")
                .email("maria@example.com")
                .passwordActual("PasswordActual123*")
                .passwordNuevo("PasswordNuevo456*")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("PasswordActual123*", "$2a$10$hashedActual")).thenReturn(true);
        when(passwordEncoder.encode("PasswordNuevo456*")).thenReturn("$2a$10$hashedNuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(usuarioMapper.toResponseDto(usuarioMock)).thenReturn(
                UsuarioResponseDto.builder().id(1L).email("maria@example.com").build()
        );

        UsuarioResponseDto respuesta = usuarioService.actualizarPerfil("maria@example.com", request);

        assertNotNull(respuesta);
        verify(passwordEncoder, times(1)).matches("PasswordActual123*", "$2a$10$hashedActual");
        verify(passwordEncoder, times(1)).encode("PasswordNuevo456*");
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si la contrasena actual es incorrecta")
    void actualizarPerfilPasswordActualIncorrecta() {
        ActualizarPerfilRequestDto request = ActualizarPerfilRequestDto.builder()
                .nombre("Maria Gomez")
                .email("maria@example.com")
                .passwordActual("ClaveIncorrecta")
                .passwordNuevo("PasswordNuevo456*")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("ClaveIncorrecta", "$2a$10$hashedActual")).thenReturn(false);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> usuarioService.actualizarPerfil("maria@example.com", request)
        );

        assertEquals("La contraseña actual no es correcta", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar EmailAlreadyExistException si el nuevo correo ya pertenece a otro usuario")
    void actualizarPerfilEmailDuplicado() {
        ActualizarPerfilRequestDto request = ActualizarPerfilRequestDto.builder()
                .nombre("Maria Gomez")
                .email("otro.correo@example.com")
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.existsByEmail("otro.correo@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistException.class,
                () -> usuarioService.actualizarPerfil("maria@example.com", request)
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar perfil y ubicacion de residencia exitosamente")
    void actualizarPerfilConUbicacionExitoso() {
        UbicacionRequestDto nuevaUbicacionDto = UbicacionRequestDto.builder()
                .departamento("Caldas")
                .municipio("Manizales")
                .direccionResidencia("Carrera 23 # 45-67")
                .build();

        Ubicacion nuevaUbicacionVo = new Ubicacion("Caldas", "Manizales", "Carrera 23 # 45-67");

        ActualizarPerfilRequestDto request = ActualizarPerfilRequestDto.builder()
                .nombre("Maria Gomez Actualizado")
                .email("maria@example.com")
                .telefono("3101112233")
                .ubicacion(nuevaUbicacionDto)
                .build();

        when(usuarioRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(usuarioMock));
        when(usuarioMapper.toUbicacion(nuevaUbicacionDto)).thenReturn(nuevaUbicacionVo);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(usuarioMapper.toResponseDto(usuarioMock)).thenReturn(
                UsuarioResponseDto.builder()
                        .id(1L)
                        .nombre("Maria Gomez Actualizado")
                        .email("maria@example.com")
                        .ubicacion(UbicacionResponseDto.builder()
                                .departamento("Caldas")
                                .municipio("Manizales")
                                .direccionResidencia("Carrera 23 # 45-67")
                                .build())
                        .build()
        );

        UsuarioResponseDto respuesta = usuarioService.actualizarPerfil("maria@example.com", request);

        assertNotNull(respuesta);
        assertEquals("Maria Gomez Actualizado", respuesta.getNombre());
        assertNotNull(respuesta.getUbicacion());
        assertEquals("Caldas", respuesta.getUbicacion().getDepartamento());
        assertEquals("Manizales", respuesta.getUbicacion().getMunicipio());
        verify(usuarioMapper, times(1)).toUbicacion(nuevaUbicacionDto);
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }
}
