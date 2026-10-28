package redalerta_backend.modules.auth;

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
import redalerta_backend.config.security.JwtService;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.service.impl.AuthServiceImpl;
import redalerta_backend.modules.usuario.dto.CaracterizacionRequestDto;
import redalerta_backend.modules.usuario.dto.DocumentoRequestDto;
import redalerta_backend.modules.usuario.dto.UbicacionRequestDto;
import redalerta_backend.modules.usuario.dto.UbicacionResponseDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;
import redalerta_backend.modules.usuario.repository.CaracterizacionUsuarioRepository;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Genero;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistroTest {

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private CaracterizacionUsuarioRepository caracterizacionRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private UsuarioMapper usuarioMapper;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthServiceImpl authService;

        private RegistroRequestDto requestDto;
        private UbicacionRequestDto ubicacionDto;
        private UbicacionResponseDto ubicacionResponseDto;
        private Ubicacion ubicacionVO;

        @BeforeEach
        void setUp() {
                CaracterizacionRequestDto caracDto = CaracterizacionRequestDto.builder()
                                .fechaNacimiento(LocalDate.of(1995, 5, 20))
                                .genero(Genero.FEMENINO)
                                .enGestacionLactancia(true)
                                .numeroIntegrantesFamilia(4)
                                .tieneMenoresDeEdad(true)
                                .cantidadMenores(2)
                                .tieneMiembroDiscapacidad(false)
                                .perteneceGrupoEtnico(false)
                                .build();

                ubicacionDto = UbicacionRequestDto.builder()
                                .departamento("Quindio")
                                .municipio("Armenia")
                                .direccionResidencia("Calle 10 # 20-30")
                                .build();

                ubicacionResponseDto = UbicacionResponseDto.builder()
                                .departamento("Quindio")
                                .municipio("Armenia")
                                .direccionResidencia("Calle 10 # 20-30")
                                .build();

                ubicacionVO = new Ubicacion("Quindio", "Armenia", "Calle 10 # 20-30");

                requestDto = RegistroRequestDto.builder()
                                .nombre("Maria Gomez")
                                .documento(new DocumentoRequestDto("1094857291", TipoDeDocumento.CEDULA))
                                .ubicacion(ubicacionDto)
                                .email("maria@example.com")
                                .password("Password123*")
                                .telefono("3109876543")
                                .caracterizacion(caracDto)
                                .build();
        }

        @Test
        @DisplayName("Debe registrar un nuevo usuario exitosamente con rol CIUDADANO y caracterización")
        void registrarUsuarioExitoso() {
                when(usuarioRepository.existsByEmail(any(String.class))).thenReturn(false);
                when(usuarioRepository.existsByDocumento(any(Documento.class))).thenReturn(false);
                when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("$2a$10$encodedPassword");
                when(usuarioMapper.toUbicacion(any(UbicacionRequestDto.class))).thenReturn(ubicacionVO);

                Usuario usuarioGuardado = Usuario.builder()
                                .id(1L)
                                .nombre("Maria Gomez")
                                .email("maria@example.com")
                                .documento(new Documento("1094857291", TipoDeDocumento.CEDULA))
                                .ubicacion(ubicacionVO)
                                .password("$2a$10$encodedPassword")
                                .rol(Rol.ROLE_CIUDADANO)
                                .activo(true)
                                .build();

                when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
                when(caracterizacionRepository.save(any(CaracterizacionUsuario.class))).thenAnswer(i -> i.getArgument(0));
                when(usuarioMapper.toResponseDto(eq(usuarioGuardado), any(CaracterizacionUsuario.class))).thenReturn(
                                UsuarioResponseDto.builder()
                                                .id(1L)
                                                .nombre("Maria Gomez")
                                                .email("maria@example.com")
                                                .ubicacion(ubicacionResponseDto)
                                                .rol(Rol.ROLE_CIUDADANO)
                                                .activo(true)
                                                .build());

                UsuarioResponseDto respuesta = authService.registrar(requestDto);

                assertNotNull(respuesta);
                assertEquals(1L, respuesta.getId());
                assertEquals("maria@example.com", respuesta.getEmail());
                assertEquals(Rol.ROLE_CIUDADANO, respuesta.getRol());
                verify(usuarioRepository, times(1)).save(any(Usuario.class));
                verify(caracterizacionRepository, times(1)).save(any(CaracterizacionUsuario.class));
        }

        @Test
        @DisplayName("Debe lanzar EmailAlreadyExistException si el email ya existe")
        void registrarEmailDuplicado() {
                when(usuarioRepository.existsByEmail(any(String.class))).thenReturn(true);

                EmailAlreadyExistException exception = assertThrows(
                                EmailAlreadyExistException.class,
                                () -> authService.registrar(requestDto));

                assertTrue(exception.getMessage().contains("maria@example.com"));
                verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si el documento ya se encuentra registrado")
        void registrarDocumentoDuplicado() {
                when(usuarioRepository.existsByEmail(any(String.class))).thenReturn(false);
                when(usuarioRepository.existsByDocumento(any(Documento.class))).thenReturn(true);

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> authService.registrar(requestDto));

                assertEquals("El documento de identidad ya se encuentra registrado", exception.getMessage());
                verify(usuarioRepository, never()).save(any(Usuario.class));
        }
}
