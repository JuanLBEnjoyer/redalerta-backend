package redalerta_backend.modules.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.UbicacionAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.repository.AlertaRepository;
import redalerta_backend.modules.alerta.service.impl.AlertaServiceImpl;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlertaMapper alertaMapper;

    @InjectMocks
    private AlertaServiceImpl alertaService;

    private Usuario usuarioCiudadano;
    private CrearAlertaRequestDto requestDto;
    private UbicacionAlerta ubicacionVO;
    private UbicacionAlertaDto ubicacionDto;

    @BeforeEach
    void setUp() {
        usuarioCiudadano = Usuario.builder()
                .id(1L)
                .nombre("Carlos Perez")
                .email("carlos@example.com")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        ubicacionDto = UbicacionAlertaDto.builder()
                .departamento("Quindio")
                .municipio("Armenia")
                .direccionDetallada("Carrera 14 # 10-20")
                .build();

        ubicacionVO = new UbicacionAlerta("Quindio", "Armenia", "Carrera 14 # 10-20");

        requestDto = CrearAlertaRequestDto.builder()
                .descripcion("Cierre temporal de via por derrumbe.")
                .ubicacion(ubicacionDto)
                .build();
    }

    @Test
    @DisplayName("Debe crear una alerta exitosamente sin evidencias")
    void crearAlertaExitosoSinEvidencias() {
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaMapper.toUbicacionAlerta(ubicacionDto)).thenReturn(ubicacionVO);

        Alerta alertaGuardada = Alerta.builder()
                .id(10L)
                .descripcion(requestDto.getDescripcion())
                .ubicacion(ubicacionVO)
                .autor(usuarioCiudadano)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .build();

        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaGuardada);
        when(alertaMapper.toResponseDto(alertaGuardada)).thenReturn(
                AlertaResponseDto.builder()
                        .id(10L)
                        .descripcion(requestDto.getDescripcion())
                        .ubicacion(ubicacionDto)
                        .autorId(1L)
                        .autorNombre("Carlos Perez")
                        .estado(EstadoAlerta.PENDIENTE_REVISION)
                        .build()
        );

        AlertaResponseDto respuesta = alertaService.crearAlerta(requestDto, "carlos@example.com");

        assertNotNull(respuesta);
        assertEquals(10L, respuesta.getId());
        assertEquals("Carlos Perez", respuesta.getAutorNombre());
        assertEquals(EstadoAlerta.PENDIENTE_REVISION, respuesta.getEstado());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Debe crear una alerta exitosamente con evidencias fotograficas")
    void crearAlertaExitosoConEvidencias() {
        requestDto.setEvidenciasUrls(List.of("https://s3.amazonaws.com/redalerta/evidencia1.jpg"));

        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaMapper.toUbicacionAlerta(ubicacionDto)).thenReturn(ubicacionVO);

        Alerta alertaGuardada = Alerta.builder()
                .id(11L)
                .descripcion(requestDto.getDescripcion())
                .ubicacion(ubicacionVO)
                .autor(usuarioCiudadano)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .build();

        when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaGuardada);
        when(alertaMapper.toResponseDto(alertaGuardada)).thenReturn(
                AlertaResponseDto.builder()
                        .id(11L)
                        .descripcion(requestDto.getDescripcion())
                        .estado(EstadoAlerta.PENDIENTE_REVISION)
                        .build()
        );

        AlertaResponseDto respuesta = alertaService.crearAlerta(requestDto, "carlos@example.com");

        assertNotNull(respuesta);
        assertEquals(11L, respuesta.getId());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si el usuario se encuentra inactivo")
    void crearAlertaUsuarioInactivo() {
        usuarioCiudadano.setActivo(false);
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> alertaService.crearAlerta(requestDto, "carlos@example.com")
        );

        assertEquals("El usuario se encuentra inactivo", exception.getMessage());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
    void crearAlertaUsuarioNoExiste() {
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> alertaService.crearAlerta(requestDto, "carlos@example.com")
        );

        assertTrue(exception.getMessage().contains("carlos@example.com"));
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    @DisplayName("Debe listar las alertas del ciudadano de forma paginada")
    void listarMisAlertasExitoso() {
        Pageable pageable = PageRequest.of(0, 10);
        Alerta alerta = Alerta.builder()
                .id(1L)
                .descripcion("Incidente en la via")
                .autor(usuarioCiudadano)
                .build();

        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaRepository.findByAutorIdOrderByFechaCreacionDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(alerta)));
        when(alertaMapper.toResponseDto(alerta)).thenReturn(
                AlertaResponseDto.builder().id(1L).descripcion("Incidente en la via").build()
        );

        Page<AlertaResponseDto> resultado = alertaService.listarMisAlertas("carlos@example.com", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(1L, resultado.getContent().getFirst().getId());
    }

    @Test
    @DisplayName("Debe obtener el detalle de una alerta especifica si pertenece al ciudadano")
    void obtenerAlertaPorIdExitoso() {
        Alerta alerta = Alerta.builder()
                .id(5L)
                .descripcion("Alerta propia")
                .autor(usuarioCiudadano)
                .build();

        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(alertaMapper.toResponseDto(alerta)).thenReturn(
                AlertaResponseDto.builder().id(5L).descripcion("Alerta propia").build()
        );

        AlertaResponseDto respuesta = alertaService.obtenerAlertaPorId(5L, "carlos@example.com");

        assertNotNull(respuesta);
        assertEquals(5L, respuesta.getId());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si un ciudadano intenta consultar una alerta ajena")
    void obtenerAlertaPorIdAccesoDenegadoOtroUsuario() {
        Usuario otroUsuario = Usuario.builder().id(99L).build();
        Alerta alertaAjena = Alerta.builder()
                .id(5L)
                .descripcion("Alerta de otro ciudadano")
                .autor(otroUsuario)
                .build();

        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alertaAjena));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> alertaService.obtenerAlertaPorId(5L, "carlos@example.com")
        );

        assertEquals("No tiene permisos para acceder a esta alerta", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la alerta no existe")
    void obtenerAlertaPorIdNoExiste() {
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
        when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> alertaService.obtenerAlertaPorId(999L, "carlos@example.com")
        );

        assertTrue(exception.getMessage().contains("999"));
    }
}
