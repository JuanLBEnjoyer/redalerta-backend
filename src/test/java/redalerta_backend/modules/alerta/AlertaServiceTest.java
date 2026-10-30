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
import org.springframework.data.jpa.domain.Specification;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CancelarDireccionamientoRequestDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.DireccionarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.IniciarGestionRequestDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.OrganizacionResponseDto;
import redalerta_backend.modules.alerta.dto.RechazarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.ResolverAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.UbicacionAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.repository.AlertaRepository;
import redalerta_backend.modules.alerta.service.impl.AlertaServiceImpl;
import redalerta_backend.modules.alerta.valueobject.CategoriaAlerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.PrioridadAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import org.mockito.ArgumentCaptor;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import redalerta_backend.common.storage.StorageService;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

        @Mock
        private AlertaRepository alertaRepository;

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private AlertaMapper alertaMapper;

        @Mock
        private StorageService storageService;

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
                                .latitud(4.5338)
                                .longitud(-75.6811)
                                .altitud(1480.0)
                                .build();

                ubicacionVO = new UbicacionAlerta("Quindio", "Armenia", "Carrera 14 # 10-20", 4.5338, -75.6811, 1480.0);

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
                                                .build());

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
                                                .build());

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
                                () -> alertaService.crearAlerta(requestDto, "carlos@example.com"));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(alertaRepository, never()).save(any(Alerta.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario no existe")
        void crearAlertaUsuarioNoExiste() {
                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertaService.crearAlerta(requestDto, "carlos@example.com"));

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
                when(alertaMapper.toResumenResponseDto(alerta, 3)).thenReturn(
                                AlertaResponseDto.builder().id(1L).descripcion("Incidente en la via").totalEvidencias(0)
                                                .build());

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
                                AlertaResponseDto.builder().id(5L).descripcion("Alerta propia").build());

                AlertaResponseDto respuesta = alertaService.obtenerAlertaPorId(5L, "carlos@example.com");

                assertNotNull(respuesta);
                assertEquals(5L, respuesta.getId());
        }

        @Test
        @DisplayName("Debe permitir a cualquier usuario autenticado consultar el detalle de una alerta ajena")
        void obtenerAlertaPorIdAlertaAjenaExitoso() {
                Usuario otroUsuario = Usuario.builder().id(99L).nombre("Maria Lopez").build();
                Alerta alertaAjena = Alerta.builder()
                                .id(5L)
                                .descripcion("Alerta de otro ciudadano")
                                .autor(otroUsuario)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(5L)).thenReturn(Optional.of(alertaAjena));
                when(alertaMapper.toResponseDto(alertaAjena)).thenReturn(
                                AlertaResponseDto.builder().id(5L).descripcion("Alerta de otro ciudadano")
                                                .autorNombre("Maria Lopez").build());

                AlertaResponseDto respuesta = alertaService.obtenerAlertaPorId(5L, "carlos@example.com");

                assertNotNull(respuesta);
                assertEquals(5L, respuesta.getId());
                assertEquals("Maria Lopez", respuesta.getAutorNombre());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario consultante esta inactivo al obtener detalle")
        void obtenerAlertaPorIdUsuarioInactivo() {
                Usuario usuarioInactivo = Usuario.builder()
                                .id(9L)
                                .email("inactivo@example.com")
                                .activo(false)
                                .build();

                when(usuarioRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(usuarioInactivo));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.obtenerAlertaPorId(5L, "inactivo@example.com"));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(alertaRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la alerta no existe")
        void obtenerAlertaPorIdNoExiste() {
                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertaService.obtenerAlertaPorId(999L, "carlos@example.com"));

                assertTrue(exception.getMessage().contains("999"));
        }

        @Test
        @DisplayName("Debe eliminar la alerta exitosamente si pertenece al ciudadano y esta en PENDIENTE_REVISION")
        void eliminarAlertaExitoso() {
                Alerta alerta = Alerta.builder()
                                .id(10L)
                                .descripcion("Alerta para eliminar")
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(10L)).thenReturn(Optional.of(alerta));

                alertaService.eliminarAlerta(10L, "carlos@example.com");

                verify(alertaRepository, times(1)).delete(alerta);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar una alerta inexistente")
        void eliminarAlertaNoExiste() {
                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertaService.eliminarAlerta(999L, "carlos@example.com"));

                assertTrue(exception.getMessage().contains("999"));
                verify(alertaRepository, never()).delete(any(Alerta.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un usuario inactivo intenta eliminar una alerta")
        void eliminarAlertaUsuarioInactivo() {
                Usuario usuarioInactivo = Usuario.builder()
                                .id(2L)
                                .email("inactivo@example.com")
                                .activo(false)
                                .build();

                when(usuarioRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(usuarioInactivo));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.eliminarAlerta(10L, "inactivo@example.com"));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(alertaRepository, never()).delete(any(Alerta.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un ciudadano intenta eliminar la alerta de otro usuario")
        void eliminarAlertaAccesoDenegadoOtroUsuario() {
                Usuario otroUsuario = Usuario.builder().id(99L).build();
                Alerta alertaAjena = Alerta.builder()
                                .id(10L)
                                .descripcion("Alerta de otro usuario")
                                .autor(otroUsuario)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(10L)).thenReturn(Optional.of(alertaAjena));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.eliminarAlerta(10L, "carlos@example.com"));

                assertEquals("No tiene permisos para eliminar esta alerta", exception.getMessage());
                verify(alertaRepository, never()).delete(any(Alerta.class));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la alerta ya no esta en PENDIENTE_REVISION")
        void eliminarAlertaEstadoNoValido() {
                Alerta alertaEnGestion = Alerta.builder()
                                .id(10L)
                                .descripcion("Alerta en gestion")
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.EN_GESTION)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(10L)).thenReturn(Optional.of(alertaEnGestion));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> alertaService.eliminarAlerta(10L, "carlos@example.com"));

                assertEquals("Solo se pueden eliminar alertas en estado PENDIENTE_REVISION", exception.getMessage());
                verify(alertaRepository, never()).delete(any(Alerta.class));
        }

        @Test
        @DisplayName("Debe modificar la alerta exitosamente si pertenece al ciudadano y esta en PENDIENTE_REVISION")
        void modificarAlertaExitosoConEvidencias() {
                Alerta alertaOriginal = Alerta.builder()
                                .id(15L)
                                .descripcion("Descripcion original")
                                .ubicacion(ubicacionVO)
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                UbicacionAlertaDto nuevaUbicacionDto = UbicacionAlertaDto.builder()
                                .departamento("Risaralda")
                                .municipio("Pereira")
                                .direccionDetallada("Carrera 7 # 20-30")
                                .latitud(4.8133)
                                .longitud(-75.6961)
                                .altitud(1411.0)
                                .build();

                UbicacionAlerta nuevaUbicacionVO = new UbicacionAlerta("Risaralda", "Pereira", "Carrera 7 # 20-30", 4.8133, -75.6961, 1411.0);

                ModificarAlertaRequestDto request = ModificarAlertaRequestDto.builder()
                                .descripcion("Descripcion actualizada del incidente")
                                .ubicacion(nuevaUbicacionDto)
                                .evidenciasUrls(List.of("https://s3.amazonaws.com/redalerta/nueva_foto.jpg"))
                                .build();

                AlertaResponseDto responseDto = AlertaResponseDto.builder()
                                .id(15L)
                                .descripcion("Descripcion actualizada del incidente")
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(15L)).thenReturn(Optional.of(alertaOriginal));
                when(alertaMapper.toUbicacionAlerta(nuevaUbicacionDto)).thenReturn(nuevaUbicacionVO);
                when(alertaRepository.save(any(Alerta.class))).thenReturn(alertaOriginal);
                when(alertaMapper.toResponseDto(alertaOriginal)).thenReturn(responseDto);

                AlertaResponseDto resultado = alertaService.modificarAlerta(15L, request, "carlos@example.com");

                assertNotNull(resultado);
                assertEquals(15L, resultado.getId());
                assertEquals("Descripcion actualizada del incidente", resultado.getDescripcion());
                verify(alertaRepository, times(1)).save(alertaOriginal);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException al intentar modificar una alerta inexistente")
        void modificarAlertaNoExiste() {
                ModificarAlertaRequestDto request = ModificarAlertaRequestDto.builder()
                                .descripcion("Descripcion actualizada")
                                .ubicacion(ubicacionDto)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertaService.modificarAlerta(999L, request, "carlos@example.com"));

                assertTrue(exception.getMessage().contains("999"));
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un usuario inactivo intenta modificar una alerta")
        void modificarAlertaUsuarioInactivo() {
                Usuario usuarioInactivo = Usuario.builder()
                                .id(2L)
                                .email("inactivo@example.com")
                                .activo(false)
                                .build();

                ModificarAlertaRequestDto request = ModificarAlertaRequestDto.builder()
                                .descripcion("Descripcion actualizada")
                                .ubicacion(ubicacionDto)
                                .build();

                when(usuarioRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(usuarioInactivo));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.modificarAlerta(15L, request, "inactivo@example.com"));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un ciudadano intenta modificar una alerta de otro usuario")
        void modificarAlertaAccesoDenegadoOtroUsuario() {
                Usuario otroUsuario = Usuario.builder().id(99L).build();
                Alerta alertaAjena = Alerta.builder()
                                .id(15L)
                                .descripcion("Alerta de otro ciudadano")
                                .autor(otroUsuario)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                ModificarAlertaRequestDto request = ModificarAlertaRequestDto.builder()
                                .descripcion("Intento de modificar alerta ajena")
                                .ubicacion(ubicacionDto)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(15L)).thenReturn(Optional.of(alertaAjena));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.modificarAlerta(15L, request, "carlos@example.com"));

                assertEquals("No tiene permisos para modificar esta alerta", exception.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la alerta a modificar ya no esta en PENDIENTE_REVISION")
        void modificarAlertaEstadoNoValido() {
                Alerta alertaEnGestion = Alerta.builder()
                                .id(15L)
                                .descripcion("Alerta ya en gestion")
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.EN_GESTION)
                                .build();

                ModificarAlertaRequestDto request = ModificarAlertaRequestDto.builder()
                                .descripcion("Intento de modificar alerta en gestion")
                                .ubicacion(ubicacionDto)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(15L)).thenReturn(Optional.of(alertaEnGestion));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> alertaService.modificarAlerta(15L, request, "carlos@example.com"));

                assertEquals("Solo se pueden modificar alertas en estado PENDIENTE_REVISION", exception.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe listar alertas aplicando criterios de filtrado territorial, estado y fechas")
        void listarAlertasConFiltrosCompletosExitoso() {
                Pageable pageable = PageRequest.of(0, 10);
                FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                                .departamento("Quindio")
                                .municipio("Armenia")
                                .estado(EstadoAlerta.EN_GESTION)
                                .fechaInicio(LocalDate.now().minusDays(5))
                                .fechaFin(LocalDate.now())
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(3L)
                                .descripcion("Inundacion en sector norte")
                                .estado(EstadoAlerta.EN_GESTION)
                                .autor(usuarioCiudadano)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findAll(ArgumentMatchers.<Specification<Alerta>>any(), eq(pageable)))
                                .thenReturn(new PageImpl<>(List.of(alerta)));
                when(alertaMapper.toResumenResponseDto(alerta, 3)).thenReturn(
                                AlertaResponseDto.builder().id(3L).descripcion("Inundacion en sector norte")
                                                .estado(EstadoAlerta.EN_GESTION).totalEvidencias(0).build());

                Page<AlertaResponseDto> resultado = alertaService.listarAlertas(filtro, "carlos@example.com", pageable);

                assertNotNull(resultado);
                assertEquals(1, resultado.getContent().size());
                assertEquals(3L, resultado.getContent().getFirst().getId());
                assertEquals(EstadoAlerta.EN_GESTION, resultado.getContent().getFirst().getEstado());
                verify(alertaRepository).findAll(ArgumentMatchers.<Specification<Alerta>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la fecha de inicio es posterior a la fecha de fin")
        void listarAlertasFechaInicioPosteriorAFechaFin() {
                Pageable pageable = PageRequest.of(0, 10);
                FiltroAlertaDto filtroInvalido = FiltroAlertaDto.builder()
                                .fechaInicio(LocalDate.of(2026, 10, 20))
                                .fechaFin(LocalDate.of(2026, 10, 10))
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> alertaService.listarAlertas(filtroInvalido, "carlos@example.com", pageable));

                assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin", exception.getMessage());
                verify(alertaRepository, never()).findAll(ArgumentMatchers.<Specification<Alerta>>any(),
                                any(Pageable.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si el usuario consultante no existe al listar alertas")
        void listarAlertasUsuarioNoExiste() {
                Pageable pageable = PageRequest.of(0, 10);
                when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

                ResourceNotFoundException exception = assertThrows(
                                ResourceNotFoundException.class,
                                () -> alertaService.listarAlertas(null, "noexiste@example.com", pageable));

                assertTrue(exception.getMessage().contains("noexiste@example.com"));
                verify(alertaRepository, never()).findAll(ArgumentMatchers.<Specification<Alerta>>any(),
                                any(Pageable.class));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el usuario consultante esta inactivo al listar alertas")
        void listarAlertasUsuarioInactivo() {
                Pageable pageable = PageRequest.of(0, 10);
                Usuario usuarioInactivo = Usuario.builder()
                                .id(9L)
                                .email("inactivo@example.com")
                                .activo(false)
                                .build();

                when(usuarioRepository.findByEmail("inactivo@example.com")).thenReturn(Optional.of(usuarioInactivo));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.listarAlertas(null, "inactivo@example.com", pageable));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(alertaRepository, never()).findAll(ArgumentMatchers.<Specification<Alerta>>any(),
                                any(Pageable.class));
        }

        @Test
        @DisplayName("Debe crear una alerta con indicador de anonimato activo")
        void crearAlertaAnonimaExitoso() {
                CrearAlertaRequestDto requestAnonimo = CrearAlertaRequestDto.builder()
                                .descripcion("Robo en curso cerca al parque principal")
                                .ubicacion(ubicacionDto)
                                .esAnonimo(true)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaMapper.toUbicacionAlerta(ubicacionDto)).thenReturn(ubicacionVO);
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenAnswer(invocation -> {
                        Alerta a = invocation.getArgument(0);
                        return AlertaResponseDto.builder()
                                        .id(100L)
                                        .descripcion(a.getDescripcion())
                                        .autorNombre("Anónimo")
                                        .autorId(null)
                                        .esAnonimo(a.getEsAnonimo())
                                        .build();
                });

                AlertaResponseDto respuesta = alertaService.crearAlerta(requestAnonimo, "carlos@example.com");

                assertNotNull(respuesta);
                assertTrue(respuesta.getEsAnonimo());
                assertEquals("Anónimo", respuesta.getAutorNombre());
                assertNull(respuesta.getAutorId());

                ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
                verify(alertaRepository).save(alertaCaptor.capture());
                assertTrue(alertaCaptor.getValue().getEsAnonimo());
        }

        @Test
        @DisplayName("Debe permitir modificar el indicador de anonimato de una alerta en PENDIENTE_REVISION")
        void modificarAlertaAnonimatoExitoso() {
                Alerta alertaOriginal = Alerta.builder()
                                .id(20L)
                                .descripcion("Descripcion original")
                                .ubicacion(ubicacionVO)
                                .autor(usuarioCiudadano)
                                .esAnonimo(false)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                ModificarAlertaRequestDto requestModificar = ModificarAlertaRequestDto.builder()
                                .descripcion("Descripcion modificada")
                                .ubicacion(ubicacionDto)
                                .esAnonimo(true)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(20L)).thenReturn(Optional.of(alertaOriginal));
                when(alertaMapper.toUbicacionAlerta(ubicacionDto)).thenReturn(ubicacionVO);
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(
                                AlertaResponseDto.builder().id(20L).esAnonimo(true).autorNombre("Anónimo").build());

                AlertaResponseDto resultado = alertaService.modificarAlerta(20L, requestModificar,
                                "carlos@example.com");

                assertNotNull(resultado);
                assertTrue(resultado.getEsAnonimo());
                assertTrue(alertaOriginal.getEsAnonimo());
                verify(alertaRepository).save(alertaOriginal);
        }

        @Test
        @DisplayName("Debe asignar por defecto la ubicacion del usuario y tolerar filtro nulo al listar alertas")
        void listarAlertasSinFiltroUbicacionTomaUbicacionUsuario() {
                Pageable pageable = PageRequest.of(0, 10);
                Ubicacion ubicacionUsuario = new Ubicacion("Antioquia", "Medellin", "Calle 50 # 10-20");
                usuarioCiudadano.setUbicacion(ubicacionUsuario);

                Alerta alerta = Alerta.builder()
                                .id(1L)
                                .descripcion("Alerta en mi zona")
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .autor(usuarioCiudadano)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findAll(ArgumentMatchers.<Specification<Alerta>>any(), eq(pageable)))
                                .thenReturn(new PageImpl<>(List.of(alerta)));
                when(alertaMapper.toResumenResponseDto(alerta, 3)).thenReturn(
                                AlertaResponseDto.builder().id(1L).descripcion("Alerta en mi zona")
                                                .estado(EstadoAlerta.PENDIENTE_REVISION).totalEvidencias(0).build());

                // 1. Validar tolerancia a null (filtro == null)
                Page<AlertaResponseDto> resultadoNull = alertaService.listarAlertas(null, "carlos@example.com",
                                pageable);
                assertNotNull(resultadoNull);
                assertEquals(1, resultadoNull.getContent().size());

                // 2. Validar asignacion forzada de departamento del usuario cuando se pasa filtro vacio
                FiltroAlertaDto filtroVacio = new FiltroAlertaDto();
                Page<AlertaResponseDto> resultadoVacio = alertaService.listarAlertas(filtroVacio, "carlos@example.com",
                                pageable);
                assertNotNull(resultadoVacio);
                assertEquals("Antioquia", filtroVacio.getDepartamento());
                assertNull(filtroVacio.getMunicipio()); // Permite consultar todos los municipios del departamento

                // 3. Validar que cualquier departamento externo enviado es sobreescrito por el del usuario
                FiltroAlertaDto filtroConOtroDepto = FiltroAlertaDto.builder()
                                .departamento("Santander")
                                .municipio("Bello")
                                .build();
                Page<AlertaResponseDto> resultadoOtro = alertaService.listarAlertas(filtroConOtroDepto, "carlos@example.com",
                                pageable);
                assertNotNull(resultadoOtro);
                assertEquals("Antioquia", filtroConOtroDepto.getDepartamento());
                assertEquals("Bello", filtroConOtroDepto.getMunicipio());

                verify(alertaRepository, times(3)).findAll(ArgumentMatchers.<Specification<Alerta>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta crear una alerta con mas de 5 evidencias")
        void crearAlertaExcedeLimiteEvidenciasLanzaBadRequestException() {
                List<String> seisEvidencias = List.of(
                                "https://s3.amazonaws.com/1.jpg",
                                "https://s3.amazonaws.com/2.jpg",
                                "https://s3.amazonaws.com/3.jpg",
                                "https://s3.amazonaws.com/4.jpg",
                                "https://s3.amazonaws.com/5.jpg",
                                "https://s3.amazonaws.com/6.jpg");
                requestDto.setEvidenciasUrls(seisEvidencias);

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> alertaService.crearAlerta(requestDto, "carlos@example.com"));

                assertEquals("No se pueden adjuntar mas de 5 evidencias por alerta", exception.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta modificar una alerta con mas de 5 evidencias")
        void modificarAlertaExcedeLimiteEvidenciasLanzaBadRequestException() {
                Alerta alertaOriginal = Alerta.builder()
                                .id(15L)
                                .descripcion("Descripcion")
                                .ubicacion(ubicacionVO)
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                List<String> seisEvidencias = List.of(
                                "https://s3.amazonaws.com/1.jpg",
                                "https://s3.amazonaws.com/2.jpg",
                                "https://s3.amazonaws.com/3.jpg",
                                "https://s3.amazonaws.com/4.jpg",
                                "https://s3.amazonaws.com/5.jpg",
                                "https://s3.amazonaws.com/6.jpg");

                ModificarAlertaRequestDto requestExcedido = ModificarAlertaRequestDto.builder()
                                .descripcion("Modificacion con exceso de fotos")
                                .ubicacion(ubicacionDto)
                                .evidenciasUrls(seisEvidencias)
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(alertaRepository.findById(15L)).thenReturn(Optional.of(alertaOriginal));

                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> alertaService.modificarAlerta(15L, requestExcedido, "carlos@example.com"));

                assertEquals("No se pueden adjuntar mas de 5 evidencias por alerta", exception.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe subir evidencia exitosamente delegando al StorageService para usuario activo")
        void subirEvidenciaExitoso() {
                MockMultipartFile archivoMock = new MockMultipartFile(
                                "archivo", "foto.jpg", "image/jpeg", "dummy-image-content".getBytes());

                UploadEvidenciaResponseDto respuestaEsperada = UploadEvidenciaResponseDto.builder()
                                .url("https://bucket.s3.amazonaws.com/alertas/foto.jpg")
                                .key("alertas/foto.jpg")
                                .nombreOriginal("foto.jpg")
                                .pesoOriginalBytes(1000L)
                                .pesoComprimidoBytes(300L)
                                .tipoMime("image/jpeg")
                                .build();

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));
                when(storageService.subirEvidencia(archivoMock, "alertas/evidencias")).thenReturn(respuestaEsperada);

                UploadEvidenciaResponseDto resultado = alertaService.subirEvidencia(archivoMock, "carlos@example.com");

                assertNotNull(resultado);
                assertEquals("https://bucket.s3.amazonaws.com/alertas/foto.jpg", resultado.getUrl());
                verify(storageService).subirEvidencia(archivoMock, "alertas/evidencias");
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un usuario inactivo intenta subir evidencia")
        void subirEvidenciaUsuarioInactivoLanzaUnauthorizedException() {
                MockMultipartFile archivoMock = new MockMultipartFile(
                                "archivo", "foto.jpg", "image/jpeg", "dummy-image-content".getBytes());
                usuarioCiudadano.setActivo(false);

                when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuarioCiudadano));

                UnauthorizedException exception = assertThrows(
                                UnauthorizedException.class,
                                () -> alertaService.subirEvidencia(archivoMock, "carlos@example.com"));

                assertEquals("El usuario se encuentra inactivo", exception.getMessage());
                verify(storageService, never()).subirEvidencia(any(), any());
        }

        // ==================== HU-06: DIRECCIONAMIENTO DE ALERTAS ====================

        @Test
        @DisplayName("Debe direccionar una alerta exitosamente asignando categoria, prioridad y observacion")
        void direccionarAlertaExitoso() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .nombre("Lider Comunitario Armenia")
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Granada"))
                                .activo(true)
                                .build();

                Usuario organizacion = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Sede Comunal Barrio Granada"))
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Incendio estructural en bodega comercial")
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Av Bolivar Calle 15", 4.5400, -75.6700, 1480.0))
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.INCENDIO)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(20L)
                                .observacionTecnica("Se requiere desplazamiento urgente de brigadistas comunitarios con extintores.")
                                .build();

                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(usuarioRepository.findById(20L)).thenReturn(Optional.of(organizacion));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoEsperado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .categoria(CategoriaAlerta.INCENDIO)
                                .prioridad(PrioridadAlerta.ALTA)
                                .liderId(5L)
                                .liderNombre("Lider Comunitario Armenia")
                                .organizacionId(20L)
                                .organizacionNombre("Brigada Comunitaria Armenia")
                                .observacionTecnica(request.getObservacionTecnica())
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoEsperado);

                AlertaResponseDto resultado = alertaService.direccionarAlerta(100L, request, "lider.armenia@redalerta.gov.co");

                assertNotNull(resultado);
                assertEquals(EstadoAlerta.DIRECCIONADA, resultado.getEstado());
                assertEquals(CategoriaAlerta.INCENDIO, resultado.getCategoria());
                assertEquals(PrioridadAlerta.ALTA, resultado.getPrioridad());
                assertEquals("Brigada Comunitaria Armenia", resultado.getOrganizacionNombre());
                assertEquals("Lider Comunitario Armenia", resultado.getLiderNombre());

                assertEquals(EstadoAlerta.DIRECCIONADA, alerta.getEstado());
                assertEquals(CategoriaAlerta.INCENDIO, alerta.getCategoria());
                assertEquals(PrioridadAlerta.ALTA, alerta.getPrioridad());
                assertEquals(lider, alerta.getLiderComunitario());
                assertEquals(organizacion, alerta.getOrganizacionAsignada());
                assertFalse(alerta.getHistorial().isEmpty());
                assertEquals(EstadoAlerta.DIRECCIONADA, alerta.getHistorial().get(0).estadoResultante());
                assertEquals("Lider Comunitario Armenia", alerta.getHistorial().get(0).responsableNombre());

                verify(alertaRepository).save(alerta);
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException si la alerta no existe al direccionar")
        void direccionarAlertaNoExisteLanzaResourceNotFoundException() {
                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.INFRAESTRUCTURA_Y_SERVICIOS)
                                .prioridad(PrioridadAlerta.MEDIA)
                                .organizacionId(20L)
                                .observacionTecnica("Revision de colapso de alcantarilla.")
                                .build();

                when(usuarioRepository.findByEmail("lider@redalerta.gov.co")).thenReturn(Optional.of(Usuario.builder()
                                .id(5L)
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build()));
                when(alertaRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> alertaService.direccionarAlerta(999L, request, "lider@redalerta.gov.co"));

                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si el lider esta inactivo al direccionar")
        void direccionarAlertaLiderInactivoLanzaUnauthorizedException() {
                Usuario liderInactivo = Usuario.builder()
                                .id(5L)
                                .email("lider.inactivo@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(false)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.INFRAESTRUCTURA_Y_SERVICIOS)
                                .prioridad(PrioridadAlerta.MEDIA)
                                .organizacionId(20L)
                                .observacionTecnica("Fuga de agua potable.")
                                .build();

                when(usuarioRepository.findByEmail("lider.inactivo@redalerta.gov.co")).thenReturn(Optional.of(liderInactivo));

                UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                                () -> alertaService.direccionarAlerta(100L, request, "lider.inactivo@redalerta.gov.co"));

                assertEquals("El usuario se encuentra inactivo", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si la alerta no pertenece a la jurisdiccion territorial del lider")
        void direccionarAlertaFueraDeJurisdiccionLanzaUnauthorizedException() {
                Usuario liderCalarca = Usuario.builder()
                                .id(6L)
                                .nombre("Lider Calarca")
                                .email("lider.calarca@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Calarca", "Plaza Bolivar"))
                                .activo(true)
                                .build();

                Alerta alertaArmenia = Alerta.builder()
                                .id(100L)
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Av Bolivar", 4.5400, -75.6700, 1480.0))
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.INCENDIO)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(20L)
                                .observacionTecnica("Atencion fuera de municipio.")
                                .build();

                when(usuarioRepository.findByEmail("lider.calarca@redalerta.gov.co")).thenReturn(Optional.of(liderCalarca));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaArmenia));

                UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                                () -> alertaService.direccionarAlerta(100L, request, "lider.calarca@redalerta.gov.co"));

                assertTrue(ex.getMessage().contains("jurisdicción territorial"));
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la organizacion asignada no esta activa")
        void direccionarAlertaOrganizacionInactivaLanzaBadRequestException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(true)
                                .build();

                Usuario organizacionInactiva = Usuario.builder()
                                .id(21L)
                                .nombre("Defensa Civil Inactiva")
                                .email("defensa@redalerta.gov.co")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Calle Real"))
                                .activo(false)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 10", 4.5338, -75.6811, 1480.0))
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.DESASTRE_NATURAL)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(21L)
                                .observacionTecnica("Inundacion en el sector ribereño.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(usuarioRepository.findById(21L)).thenReturn(Optional.of(organizacionInactiva));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.direccionarAlerta(100L, request, "lider.armenia@redalerta.gov.co"));

                assertEquals("La organización seleccionada se encuentra inactiva", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la organizacion no tiene competencia territorial en el departamento")
        void direccionarAlertaOrganizacionFueraDeDepartamentoLanzaBadRequestException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(true)
                                .build();

                Usuario organizacionRisaralda = Usuario.builder()
                                .id(22L)
                                .nombre("Brigada Comunitaria Pereira")
                                .email("brigada.pereira@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Risaralda", "Pereira", "Av 30 de Agosto"))
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 10", 4.5338, -75.6811, 1480.0))
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.DESASTRE_NATURAL)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(22L)
                                .observacionTecnica("Deslizamiento de tierra en ladera.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(usuarioRepository.findById(22L)).thenReturn(Optional.of(organizacionRisaralda));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.direccionarAlerta(100L, request, "lider.armenia@redalerta.gov.co"));

                assertTrue(ex.getMessage().contains("departamento de la alerta"));
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la alerta ya fue direccionada previamente")
        void direccionarAlertaEnEstadoInvalidoLanzaBadRequestException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(true)
                                .build();

                Alerta alertaYaDireccionada = Alerta.builder()
                                .id(100L)
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 10", 4.5338, -75.6811, 1480.0))
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.INCENDIO)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(20L)
                                .observacionTecnica("Re-direccionamiento indebido.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaYaDireccionada));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.direccionarAlerta(100L, request, "lider.armenia@redalerta.gov.co"));

                assertEquals("Solo se pueden direccionar alertas en estado PENDIENTE_REVISION", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe listar organizaciones elegibles para un lider comunitario activo")
        void listarOrganizacionesElegiblesExitoso() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(true)
                                .build();

                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Sede Comunal Barrio Granada"))
                                .activo(true)
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(usuarioRepository.findByRolAndUbicacionDepartamentoAndUbicacionMunicipioAndActivoTrue(
                                eq(Rol.ROLE_ORGANIZACION), eq("Quindio"), eq("Armenia")))
                                .thenReturn(List.of(brigada));

                when(alertaMapper.toOrganizacionResponseDto(brigada)).thenReturn(
                                OrganizacionResponseDto.builder()
                                                .id(20L)
                                                .nombre("Brigada Comunitaria Armenia")
                                                .email("brigada.armenia@redalerta.org")
                                                .departamento("Quindio")
                                                .municipio("Armenia")
                                                .build());

                List<OrganizacionResponseDto> resultado = alertaService.listarOrganizacionesElegibles("lider.armenia@redalerta.gov.co");

                assertNotNull(resultado);
                assertEquals(1, resultado.size());
                assertEquals("Brigada Comunitaria Armenia", resultado.get(0).getNombre());
        }

        @Test
        @DisplayName("Debe listar las alertas direccionadas por el lider comunitario autenticado")
        void listarAlertasDireccionadasPorLiderExitoso() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Centro"))
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Alerta direccionada")
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .liderComunitario(lider)
                                .build();

                Pageable pageable = PageRequest.of(0, 10);
                Page<Alerta> pageAlertas = new PageImpl<>(List.of(alerta), pageable, 1);

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findByLiderComunitarioIdOrderByFechaCreacionDesc(5L, pageable)).thenReturn(pageAlertas);
                when(alertaMapper.toResumenResponseDto(alerta, 3)).thenReturn(
                                AlertaResponseDto.builder()
                                                .id(100L)
                                                .estado(EstadoAlerta.DIRECCIONADA)
                                                .liderId(5L)
                                                .build());

                Page<AlertaResponseDto> resultado = alertaService.listarAlertasDireccionadasPorLider("lider.armenia@redalerta.gov.co", pageable);

                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
                assertEquals(EstadoAlerta.DIRECCIONADA, resultado.getContent().get(0).getEstado());
        }

        @Test
        @DisplayName("Debe listar la bandeja de alertas asignadas a la organizacion autenticada")
        void listarBandejaOrganizacionExitoso() {
                Usuario organizacion = Usuario.builder()
                                .id(20L)
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Sede Comunal Barrio Granada"))
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Incendio estructural")
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .organizacionAsignada(organizacion)
                                .build();

                Pageable pageable = PageRequest.of(0, 10);
                Page<Alerta> pageAlertas = new PageImpl<>(List.of(alerta), pageable, 1);

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(organizacion));
                when(alertaRepository.findByOrganizacionAsignadaIdAndEstadoInOrderByFechaCreacionDesc(
                                eq(20L), any(), eq(pageable))).thenReturn(pageAlertas);
                when(alertaMapper.toResumenResponseDto(alerta, 3)).thenReturn(
                                AlertaResponseDto.builder()
                                                .id(100L)
                                                .estado(EstadoAlerta.DIRECCIONADA)
                                                .organizacionId(20L)
                                                .build());

                Page<AlertaResponseDto> resultado = alertaService.listarBandejaOrganizacion("brigada.armenia@redalerta.org", pageable);

                assertNotNull(resultado);
                assertEquals(1, resultado.getTotalElements());
                assertEquals(20L, resultado.getContent().get(0).getOrganizacionId());
        }

        @Test
        @DisplayName("Debe modificar direccionamiento exitosamente cuando la alerta esta en estado DIRECCIONADA")
        void modificarDireccionamientoExitoso() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .nombre("Lider Comunitario Armenia")
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Granada"))
                                .activo(true)
                                .build();

                Usuario comiteSeguridad = Usuario.builder()
                                .id(21L)
                                .nombre("Comite Barrial de Seguridad Armenia")
                                .email("comite.seguridad.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .ubicacion(new Ubicacion("Quindio", "Armenia", "Casa Comunal"))
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Robo en establecimiento comercial")
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 14", 4.5338, -75.6811, 1480.0))
                                .autor(usuarioCiudadano)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .liderComunitario(lider)
                                .categoria(CategoriaAlerta.INCENDIO)
                                .prioridad(PrioridadAlerta.BAJA)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.SEGURIDAD_CIUDADANA)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(21L)
                                .observacionTecnica("Se rectifica clasificacion hacia Comite Barrial de Seguridad por tratarse de convivencia.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(usuarioRepository.findById(21L)).thenReturn(Optional.of(comiteSeguridad));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoActualizado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .categoria(CategoriaAlerta.SEGURIDAD_CIUDADANA)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(21L)
                                .organizacionNombre("Comite Barrial de Seguridad Armenia")
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoActualizado);

                AlertaResponseDto resultado = alertaService.modificarDireccionamiento(100L, request, "lider.armenia@redalerta.gov.co");

                assertNotNull(resultado);
                assertEquals(CategoriaAlerta.SEGURIDAD_CIUDADANA, resultado.getCategoria());
                assertEquals(PrioridadAlerta.ALTA, resultado.getPrioridad());
                assertEquals("Comite Barrial de Seguridad Armenia", resultado.getOrganizacionNombre());

                assertEquals(CategoriaAlerta.SEGURIDAD_CIUDADANA, alerta.getCategoria());
                assertEquals(comiteSeguridad, alerta.getOrganizacionAsignada());
                assertFalse(alerta.getHistorial().isEmpty());
                assertTrue(alerta.getHistorial().get(0).motivoOAccion().contains("ha sido actualizado"));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si otro lider intenta modificar el direccionamiento")
        void modificarDireccionamientoNoEsLiderAutorLanzaUnauthorizedException() {
                Usuario liderOriginal = Usuario.builder()
                                .id(5L)
                                .email("lider.original@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Usuario otroLider = Usuario.builder()
                                .id(8L)
                                .email("otro.lider@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .liderComunitario(liderOriginal)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.SEGURIDAD_CIUDADANA)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(21L)
                                .observacionTecnica("Modificacion no autorizada.")
                                .build();

                when(usuarioRepository.findByEmail("otro.lider@redalerta.gov.co")).thenReturn(Optional.of(otroLider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));

                assertThrows(UnauthorizedException.class,
                                () -> alertaService.modificarDireccionamiento(100L, request, "otro.lider@redalerta.gov.co"));

                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta modificar direccionamiento de alerta que no esta DIRECCIONADA")
        void modificarDireccionamientoEstadoInvalidoLanzaBadRequestException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Alerta alertaPendiente = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                DireccionarAlertaRequestDto request = DireccionarAlertaRequestDto.builder()
                                .categoria(CategoriaAlerta.SEGURIDAD_CIUDADANA)
                                .prioridad(PrioridadAlerta.ALTA)
                                .organizacionId(21L)
                                .observacionTecnica("Intento de modificar alerta pendiente.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaPendiente));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.modificarDireccionamiento(100L, request, "lider.armenia@redalerta.gov.co"));

                assertEquals("Solo se puede modificar el direccionamiento de alertas en estado DIRECCIONADA", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe cancelar direccionamiento exitosamente retornando a PENDIENTE_REVISION")
        void cancelarDireccionamientoExitoso() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .nombre("Lider Comunitario Armenia")
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Derrumbe de talud sobre calzada")
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .liderComunitario(lider)
                                .organizacionAsignada(brigada)
                                .categoria(CategoriaAlerta.DESASTRE_NATURAL)
                                .prioridad(PrioridadAlerta.ALTA)
                                .observacionTecnica("Despacho de brigadistas comunitarios con herramientas.")
                                .build();

                CancelarDireccionamientoRequestDto request = CancelarDireccionamientoRequestDto.builder()
                                .motivo("Se direcciono por error a Brigada de Emergencias, el caso compete a Comite de Convivencia.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoCancelado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoCancelado);

                AlertaResponseDto resultado = alertaService.cancelarDireccionamiento(100L, request, "lider.armenia@redalerta.gov.co");

                assertNotNull(resultado);
                assertEquals(EstadoAlerta.PENDIENTE_REVISION, resultado.getEstado());

                assertEquals(EstadoAlerta.PENDIENTE_REVISION, alerta.getEstado());
                assertNull(alerta.getOrganizacionAsignada());
                assertNull(alerta.getLiderComunitario());
                assertNull(alerta.getCategoria());
                assertNull(alerta.getPrioridad());
                assertNull(alerta.getObservacionTecnica());
                assertFalse(alerta.getHistorial().isEmpty());
                assertEquals(EstadoAlerta.PENDIENTE_REVISION, alerta.getHistorial().get(0).estadoResultante());
                assertTrue(alerta.getHistorial().get(0).motivoOAccion().contains("ha sido cancelado y regresó"));

                verify(alertaRepository).save(alerta);
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si otro lider intenta cancelar el direccionamiento")
        void cancelarDireccionamientoNoEsLiderAutorLanzaUnauthorizedException() {
                Usuario liderOriginal = Usuario.builder()
                                .id(5L)
                                .email("lider.original@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Usuario otroLider = Usuario.builder()
                                .id(8L)
                                .email("otro.lider@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .liderComunitario(liderOriginal)
                                .build();

                CancelarDireccionamientoRequestDto request = CancelarDireccionamientoRequestDto.builder()
                                .motivo("Cancelacion no autorizada.")
                                .build();

                when(usuarioRepository.findByEmail("otro.lider@redalerta.gov.co")).thenReturn(Optional.of(otroLider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));

                assertThrows(UnauthorizedException.class,
                                () -> alertaService.cancelarDireccionamiento(100L, request, "otro.lider@redalerta.gov.co"));

                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta cancelar direccionamiento de alerta en estado diferente a DIRECCIONADA")
        void cancelarDireccionamientoEstadoInvalidoLanzaBadRequestException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Alerta alertaPendiente = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .build();

                CancelarDireccionamientoRequestDto request = CancelarDireccionamientoRequestDto.builder()
                                .motivo("Intento de cancelar alerta no direccionada.")
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaPendiente));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.cancelarDireccionamiento(100L, request, "lider.armenia@redalerta.gov.co"));

                assertEquals("Solo se puede cancelar el direccionamiento de alertas en estado DIRECCIONADA", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe iniciar gestion exitosamente por el comite comunitario asignado")
        void iniciarGestionExitosoPorComiteAsignado() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Derrumbe en via de acceso")
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .organizacionAsignada(brigada)
                                .build();

                IniciarGestionRequestDto request = IniciarGestionRequestDto.builder()
                                .observacionInicial("Cuadrilla comunitaria en desplazamiento.")
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoEsperado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.EN_GESTION)
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoEsperado);

                AlertaResponseDto resultado = alertaService.iniciarGestion(100L, request, "brigada.armenia@redalerta.org");

                assertNotNull(resultado);
                assertEquals(EstadoAlerta.EN_GESTION, resultado.getEstado());
                assertEquals(EstadoAlerta.EN_GESTION, alerta.getEstado());
                assertFalse(alerta.getHistorial().isEmpty());
                assertTrue(alerta.getHistorial().get(0).motivoOAccion().contains("ha sido iniciada"));
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un comite diferente intenta iniciar gestion")
        void iniciarGestionComiteNoAsignadoLanzaUnauthorizedException() {
                Usuario brigadaAsignada = Usuario.builder()
                                .id(20L)
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Usuario otroComite = Usuario.builder()
                                .id(21L)
                                .email("otro.comite@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .organizacionAsignada(brigadaAsignada)
                                .build();

                when(usuarioRepository.findByEmail("otro.comite@redalerta.org")).thenReturn(Optional.of(otroComite));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));

                UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                                () -> alertaService.iniciarGestion(100L, null, "otro.comite@redalerta.org"));

                assertTrue(ex.getMessage().contains("asignada a otro comité"));
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la alerta no se encuentra en estado DIRECCIONADA al iniciar gestion")
        void iniciarGestionAlertaNoDireccionadaLanzaBadRequestException() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alertaPendiente = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.PENDIENTE_REVISION)
                                .organizacionAsignada(brigada)
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaPendiente));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.iniciarGestion(100L, null, "brigada.armenia@redalerta.org"));

                assertEquals("Solo se puede iniciar la gestión de alertas en estado DIRECCIONADA", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe resolver alerta exitosamente con informe final por el comite asignado")
        void resolverAlertaExitosoPorComiteAsignado() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alertaEnGestion = Alerta.builder()
                                .id(100L)
                                .descripcion("Derrumbe en via de acceso")
                                .estado(EstadoAlerta.EN_GESTION)
                                .organizacionAsignada(brigada)
                                .build();

                ResolverAlertaRequestDto request = ResolverAlertaRequestDto.builder()
                                .informeResolucion("Se retiraron los escombros y se restablecio el paso vehicular comunitario.")
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaEnGestion));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoEsperado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.RESUELTA)
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoEsperado);

                AlertaResponseDto resultado = alertaService.resolverAlerta(100L, request, "brigada.armenia@redalerta.org");

                assertNotNull(resultado);
                assertEquals(EstadoAlerta.RESUELTA, resultado.getEstado());
                assertEquals(EstadoAlerta.RESUELTA, alertaEnGestion.getEstado());
                assertFalse(alertaEnGestion.getHistorial().isEmpty());
                assertTrue(alertaEnGestion.getHistorial().get(0).motivoOAccion().contains("ha sido resuelta"));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si la alerta no se encuentra EN_GESTION al intentar resolverla")
        void resolverAlertaNoEnGestionLanzaBadRequestException() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alertaDireccionada = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .organizacionAsignada(brigada)
                                .build();

                ResolverAlertaRequestDto request = ResolverAlertaRequestDto.builder()
                                .informeResolucion("Informe de prueba suficientemente extenso para la validacion.")
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaDireccionada));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.resolverAlerta(100L, request, "brigada.armenia@redalerta.org"));

                assertEquals("Solo se pueden resolver alertas que se encuentren en estado EN_GESTION", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar alerta exitosamente por el comite comunitario asignado")
        void rechazarAlertaExitosoPorComiteAsignado() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .descripcion("Presunto incidente en via publica")
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 10", 4.5338, -75.6811, 1480.0))
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .organizacionAsignada(brigada)
                                .build();

                RechazarAlertaRequestDto request = RechazarAlertaRequestDto.builder()
                                .motivo("Se verifico en sitio y no existe tal novedad reportada.")
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));
                when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AlertaResponseDto dtoEsperado = AlertaResponseDto.builder()
                                .id(100L)
                                .estado(EstadoAlerta.RECHAZADA)
                                .build();
                when(alertaMapper.toResponseDto(any(Alerta.class))).thenReturn(dtoEsperado);

                AlertaResponseDto resultado = alertaService.rechazarAlerta(100L, request, "brigada.armenia@redalerta.org");

                assertNotNull(resultado);
                assertEquals(EstadoAlerta.RECHAZADA, resultado.getEstado());
                assertEquals(EstadoAlerta.RECHAZADA, alerta.getEstado());
                assertFalse(alerta.getHistorial().isEmpty());
                assertTrue(alerta.getHistorial().get(0).motivoOAccion().contains("ha sido rechazada"));
        }

        @Test
        @DisplayName("Debe lanzar BadRequestException si se intenta rechazar una alerta que ya esta RESUELTA")
        void rechazarAlertaYaResueltaLanzaBadRequestException() {
                Usuario brigada = Usuario.builder()
                                .id(20L)
                                .nombre("Brigada Comunitaria Armenia")
                                .email("brigada.armenia@redalerta.org")
                                .rol(Rol.ROLE_ORGANIZACION)
                                .activo(true)
                                .build();

                Alerta alertaResuelta = Alerta.builder()
                                .id(100L)
                                .ubicacion(new UbicacionAlerta("Quindio", "Armenia", "Calle 10", 4.5338, -75.6811, 1480.0))
                                .estado(EstadoAlerta.RESUELTA)
                                .organizacionAsignada(brigada)
                                .build();

                RechazarAlertaRequestDto request = RechazarAlertaRequestDto.builder()
                                .motivo("Motivo de rechazo de prueba para alerta resuelta.")
                                .build();

                when(usuarioRepository.findByEmail("brigada.armenia@redalerta.org")).thenReturn(Optional.of(brigada));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alertaResuelta));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> alertaService.rechazarAlerta(100L, request, "brigada.armenia@redalerta.org"));

                assertEquals("No es posible rechazar una alerta que ya se encuentra finalizada", ex.getMessage());
                verify(alertaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar UnauthorizedException si un lider comunitario intenta gestionar alertas")
        void operacionesGestionPorLiderLanzaUnauthorizedException() {
                Usuario lider = Usuario.builder()
                                .id(5L)
                                .email("lider.armenia@redalerta.gov.co")
                                .rol(Rol.ROLE_LIDER)
                                .activo(true)
                                .build();

                Alerta alerta = Alerta.builder()
                                .id(100L)
                                .estado(EstadoAlerta.DIRECCIONADA)
                                .build();

                when(usuarioRepository.findByEmail("lider.armenia@redalerta.gov.co")).thenReturn(Optional.of(lider));
                when(alertaRepository.findById(100L)).thenReturn(Optional.of(alerta));

                IniciarGestionRequestDto reqIniciar = IniciarGestionRequestDto.builder()
                                .observacionInicial("Intento de iniciar gestión por líder.")
                                .build();

                ResolverAlertaRequestDto reqResolver = ResolverAlertaRequestDto.builder()
                                .informeResolucion("Informe de resolución suficientemente extenso para la prueba.")
                                .build();

                RechazarAlertaRequestDto reqRechazar = RechazarAlertaRequestDto.builder()
                                .motivo("Motivo de rechazo por parte de un líder.")
                                .build();

                UnauthorizedException exIniciar = assertThrows(UnauthorizedException.class,
                                () -> alertaService.iniciarGestion(100L, reqIniciar, "lider.armenia@redalerta.gov.co"));
                assertTrue(exIniciar.getMessage().contains("Rol no autorizado"));

                UnauthorizedException exResolver = assertThrows(UnauthorizedException.class,
                                () -> alertaService.resolverAlerta(100L, reqResolver, "lider.armenia@redalerta.gov.co"));
                assertTrue(exResolver.getMessage().contains("Rol no autorizado"));

                UnauthorizedException exRechazar = assertThrows(UnauthorizedException.class,
                                () -> alertaService.rechazarAlerta(100L, reqRechazar, "lider.armenia@redalerta.gov.co"));
                assertTrue(exRechazar.getMessage().contains("Rol no autorizado"));

                verify(alertaRepository, never()).save(any());
        }
}



