package redalerta_backend.modules.admin;

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
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.admin.dto.EvidenciaSolicitudResponseDto;
import redalerta_backend.modules.admin.dto.SolicitudRegistroResponseDto;
import redalerta_backend.modules.admin.entity.SolicitudRegistro;
import redalerta_backend.modules.admin.mapper.SolicitudRegistroMapper;
import redalerta_backend.modules.admin.repository.SolicitudRegistroRepository;
import redalerta_backend.modules.admin.service.impl.AdminSolicitudServiceImpl;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSolicitudServiceTest {

    @Mock
    private SolicitudRegistroRepository solicitudRegistroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SolicitudRegistroMapper solicitudRegistroMapper;

    @InjectMocks
    private AdminSolicitudServiceImpl adminSolicitudService;

    private Usuario usuarioAdmin;
    private Usuario usuarioLider;
    private SolicitudRegistro solicitudLider;
    private SolicitudRegistroResponseDto responseDto;

    @BeforeEach
    void setUp() {
        usuarioAdmin = Usuario.builder()
                .id(1L)
                .nombre("Administrador Principal")
                .email("admin@redalerta.org")
                .rol(Rol.ROLE_ADMIN)
                .activo(true)
                .build();

        usuarioLider = Usuario.builder()
                .id(2L)
                .nombre("Jaime Restrepo")
                .email("jaime@jac.org")
                .telefono("3101234567")
                .rol(Rol.ROLE_LIDER)
                .documento(new Documento("1094857213", TipoDeDocumento.CEDULA))
                .ubicacion(new Ubicacion("Quindio", "Armenia", "Barrio Granada"))
                .activo(false)
                .build();

        solicitudLider = SolicitudRegistro.builder()
                .id(10L)
                .usuario(usuarioLider)
                .estado(EstadoSolicitud.PENDIENTE)
                .sectorComunidad("JAC Barrio Granada")
                .build();

        responseDto = SolicitudRegistroResponseDto.builder()
                .id(10L)
                .usuarioId(2L)
                .nombre("Jaime Restrepo")
                .email("jaime@jac.org")
                .rol(Rol.ROLE_LIDER)
                .estado(EstadoSolicitud.PENDIENTE)
                .sectorComunidad("JAC Barrio Granada")
                .build();
    }

    @Test
    @DisplayName("Debe listar solicitudes pendientes por defecto cuando el estado y rol son nulos")
    void listarSolicitudesPorDefectoPendienteExitoso() {
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));
        when(solicitudRegistroRepository.findByEstado(EstadoSolicitud.PENDIENTE, pageable))
                .thenReturn(new PageImpl<>(List.of(solicitudLider)));
        when(solicitudRegistroMapper.toResponseDto(solicitudLider)).thenReturn(responseDto);

        Page<SolicitudRegistroResponseDto> resultado = adminSolicitudService.listarSolicitudes(
                null, null, "admin@redalerta.org", pageable
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(10L, resultado.getContent().getFirst().getId());
        assertEquals(EstadoSolicitud.PENDIENTE, resultado.getContent().getFirst().getEstado());
        verify(solicitudRegistroRepository).findByEstado(EstadoSolicitud.PENDIENTE, pageable);
        verify(solicitudRegistroRepository, never()).findByEstadoAndUsuarioRol(any(), any(), any());
    }

    @Test
    @DisplayName("Debe listar solicitudes filtrando por rol ROLE_LIDER")
    void listarSolicitudesConFiltroRolLiderExitoso() {
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));
        when(solicitudRegistroRepository.findByEstadoAndUsuarioRol(EstadoSolicitud.PENDIENTE, Rol.ROLE_LIDER, pageable))
                .thenReturn(new PageImpl<>(List.of(solicitudLider)));
        when(solicitudRegistroMapper.toResponseDto(solicitudLider)).thenReturn(responseDto);

        Page<SolicitudRegistroResponseDto> resultado = adminSolicitudService.listarSolicitudes(
                null, Rol.ROLE_LIDER, "admin@redalerta.org", pageable
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(Rol.ROLE_LIDER, resultado.getContent().getFirst().getRol());
        verify(solicitudRegistroRepository).findByEstadoAndUsuarioRol(EstadoSolicitud.PENDIENTE, Rol.ROLE_LIDER, pageable);
    }

    @Test
    @DisplayName("Debe listar solicitudes filtrando por rol ROLE_ORGANIZACION y estado explicito")
    void listarSolicitudesConFiltroRolOrganizacionYEstadoExplicito() {
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));
        when(solicitudRegistroRepository.findByEstadoAndUsuarioRol(EstadoSolicitud.APROBADA, Rol.ROLE_ORGANIZACION, pageable))
                .thenReturn(new PageImpl<>(List.of(solicitudLider)));
        when(solicitudRegistroMapper.toResponseDto(solicitudLider)).thenReturn(responseDto);

        Page<SolicitudRegistroResponseDto> resultado = adminSolicitudService.listarSolicitudes(
                EstadoSolicitud.APROBADA, Rol.ROLE_ORGANIZACION, "admin@redalerta.org", pageable
        );

        assertNotNull(resultado);
        verify(solicitudRegistroRepository).findByEstadoAndUsuarioRol(EstadoSolicitud.APROBADA, Rol.ROLE_ORGANIZACION, pageable);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException si se intenta filtrar por ROLE_CIUDADANO")
    void listarSolicitudesRolInvalidoCiudadano() {
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> adminSolicitudService.listarSolicitudes(null, Rol.ROLE_CIUDADANO, "admin@redalerta.org", pageable)
        );

        assertTrue(exception.getMessage().contains("ROLE_LIDER o ROLE_ORGANIZACION"));
        verify(solicitudRegistroRepository, never()).findByEstado(any(), any());
        verify(solicitudRegistroRepository, never()).findByEstadoAndUsuarioRol(any(), any(), any());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el administrador no existe")
    void listarSolicitudesAdminNoExiste() {
        Pageable pageable = PageRequest.of(0, 10);
        when(usuarioRepository.findByEmail("noexiste@redalerta.org")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminSolicitudService.listarSolicitudes(null, null, "noexiste@redalerta.org", pageable)
        );

        assertTrue(exception.getMessage().contains("noexiste@redalerta.org"));
        verify(solicitudRegistroRepository, never()).findByEstado(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si el usuario administrador esta inactivo")
    void listarSolicitudesAdminInactivo() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario adminInactivo = Usuario.builder()
                .id(1L)
                .email("admin@redalerta.org")
                .rol(Rol.ROLE_ADMIN)
                .activo(false)
                .build();

        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(adminInactivo));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> adminSolicitudService.listarSolicitudes(null, null, "admin@redalerta.org", pageable)
        );

        assertEquals("El usuario se encuentra inactivo", exception.getMessage());
        verify(solicitudRegistroRepository, never()).findByEstado(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException si el usuario no tiene rol ROLE_ADMIN")
    void listarSolicitudesUsuarioNoAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario noAdmin = Usuario.builder()
                .id(5L)
                .email("ciudadano@redalerta.org")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        when(usuarioRepository.findByEmail("ciudadano@redalerta.org")).thenReturn(Optional.of(noAdmin));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> adminSolicitudService.listarSolicitudes(null, null, "ciudadano@redalerta.org", pageable)
        );

        assertEquals("No tiene permisos administrativos para acceder a este recurso", exception.getMessage());
        verify(solicitudRegistroRepository, never()).findByEstado(any(), any());
    }

    @Test
    @DisplayName("Debe consultar el detalle y evidencias de una solicitud por ID exitosamente")
    void obtenerSolicitudPorIdExitoso() {
        EvidenciaSolicitudResponseDto evidenciaDto = EvidenciaSolicitudResponseDto.builder()
                .id(1L)
                .url("https://s3.amazonaws.com/redalerta/soportes/certificado.pdf")
                .nombreArchivo("certificado.pdf")
                .fechaSubida(LocalDateTime.now())
                .build();

        SolicitudRegistroResponseDto detalleDto = SolicitudRegistroResponseDto.builder()
                .id(10L)
                .usuarioId(2L)
                .nombre("Jaime Restrepo")
                .email("jaime@jac.org")
                .rol(Rol.ROLE_LIDER)
                .estado(EstadoSolicitud.PENDIENTE)
                .sectorComunidad("JAC Barrio Granada")
                .totalEvidencias(1)
                .evidencias(List.of(evidenciaDto))
                .build();

        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));
        when(solicitudRegistroRepository.findById(10L)).thenReturn(Optional.of(solicitudLider));
        when(solicitudRegistroMapper.toResponseDto(solicitudLider)).thenReturn(detalleDto);

        SolicitudRegistroResponseDto resultado = adminSolicitudService.obtenerSolicitudPorId(10L, "admin@redalerta.org");

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("Jaime Restrepo", resultado.getNombre());
        assertEquals(Rol.ROLE_LIDER, resultado.getRol());
        assertEquals(1, resultado.getTotalEvidencias());
        assertEquals("https://s3.amazonaws.com/redalerta/soportes/certificado.pdf", resultado.getEvidencias().getFirst().getUrl());
        verify(solicitudRegistroRepository).findById(10L);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la solicitud por ID no existe")
    void obtenerSolicitudPorIdNoEncontrada() {
        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(usuarioAdmin));
        when(solicitudRegistroRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminSolicitudService.obtenerSolicitudPorId(99L, "admin@redalerta.org")
        );

        assertEquals("Solicitud de registro no encontrada con id: 99", exception.getMessage());
        verify(solicitudRegistroRepository).findById(99L);
        verify(solicitudRegistroMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException al consultar por ID si el usuario no es admin")
    void obtenerSolicitudPorIdUsuarioNoAdmin() {
        Usuario noAdmin = Usuario.builder()
                .id(5L)
                .email("ciudadano@redalerta.org")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        when(usuarioRepository.findByEmail("ciudadano@redalerta.org")).thenReturn(Optional.of(noAdmin));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> adminSolicitudService.obtenerSolicitudPorId(10L, "ciudadano@redalerta.org")
        );

        assertEquals("No tiene permisos administrativos para acceder a este recurso", exception.getMessage());
        verify(solicitudRegistroRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe lanzar UnauthorizedException al consultar por ID si el admin esta inactivo")
    void obtenerSolicitudPorIdAdminInactivo() {
        Usuario adminInactivo = Usuario.builder()
                .id(1L)
                .email("admin@redalerta.org")
                .rol(Rol.ROLE_ADMIN)
                .activo(false)
                .build();

        when(usuarioRepository.findByEmail("admin@redalerta.org")).thenReturn(Optional.of(adminInactivo));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> adminSolicitudService.obtenerSolicitudPorId(10L, "admin@redalerta.org")
        );

        assertEquals("El usuario se encuentra inactivo", exception.getMessage());
        verify(solicitudRegistroRepository, never()).findById(any());
    }
}
