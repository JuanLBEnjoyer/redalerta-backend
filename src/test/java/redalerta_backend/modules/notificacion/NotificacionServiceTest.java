package redalerta_backend.modules.notificacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.notificacion.dto.NotificacionResponseDto;
import redalerta_backend.modules.notificacion.entity.Notificacion;
import redalerta_backend.modules.notificacion.mapper.NotificacionMapper;
import redalerta_backend.modules.notificacion.repository.NotificacionRepository;
import redalerta_backend.modules.notificacion.service.impl.NotificacionServiceImpl;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacionMapper notificacionMapper;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private Usuario usuarioMock;
    private Alerta alertaMock;
    private Notificacion notificacionMock;
    private NotificacionResponseDto responseDtoMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@example.com")
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        alertaMock = Alerta.builder()
                .id(10L)
                .descripcion("Hueco peligroso en via principal")
                .estado(EstadoAlerta.DIRECCIONADA)
                .build();

        notificacionMock = Notificacion.builder()
                .id(100L)
                .usuario(usuarioMock)
                .alerta(alertaMock)
                .alertaId(10L)
                .titulo("Alerta Direccionada")
                .mensaje("Su alerta \"Hueco peligroso en via principal\" ha cambiado a estado DIRECCIONADA")
                .nuevoEstado(EstadoAlerta.DIRECCIONADA)
                .motivoAccion("Se envia a brigada para señalizacion")
                .responsableNombre("Carlos Lider")
                .responsableRol("ROLE_LIDER")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        responseDtoMock = NotificacionResponseDto.builder()
                .id(100L)
                .alertaId(10L)
                .titulo("Alerta Direccionada")
                .mensaje("Su alerta \"Hueco peligroso en via principal\" ha cambiado a estado DIRECCIONADA")
                .nuevoEstado(EstadoAlerta.DIRECCIONADA)
                .motivoAccion("Se envia a brigada para señalizacion")
                .responsableNombre("Carlos Lider")
                .responsableRol("ROLE_LIDER")
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("listarNotificaciones: Retorna todas las notificaciones del usuario paginadas")
    void listarNotificaciones_Todas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notificacion> pagina = new PageImpl<>(List.of(notificacionMock));

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuarioMock, pageable))
                .thenReturn(pagina);
        when(notificacionMapper.toResponseDto(notificacionMock)).thenReturn(responseDtoMock);

        Page<NotificacionResponseDto> resultado = notificacionService.listarNotificaciones("juan@example.com", false, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("Alerta Direccionada", resultado.getContent().get(0).getTitulo());
    }

    @Test
    @DisplayName("listarNotificaciones: Filtra solo las notificaciones no leidas")
    void listarNotificaciones_SoloNoLeidas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notificacion> pagina = new PageImpl<>(List.of(notificacionMock));

        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.findByUsuarioAndLeidaOrderByFechaCreacionDesc(usuarioMock, false, pageable))
                .thenReturn(pagina);
        when(notificacionMapper.toResponseDto(notificacionMock)).thenReturn(responseDtoMock);

        Page<NotificacionResponseDto> resultado = notificacionService.listarNotificaciones("juan@example.com", true, pageable);

        assertNotNull(resultado);
        verify(notificacionRepository).findByUsuarioAndLeidaOrderByFechaCreacionDesc(usuarioMock, false, pageable);
    }

    @Test
    @DisplayName("obtenerDetalle: Consulta detalle y marca automaticamente como leida")
    void obtenerDetalle_Exitoso_MarcaComoLeida() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.findByIdAndUsuario(100L, usuarioMock)).thenReturn(Optional.of(notificacionMock));
        when(notificacionMapper.toResponseDto(notificacionMock)).thenReturn(responseDtoMock);

        NotificacionResponseDto resultado = notificacionService.obtenerDetalle(100L, "juan@example.com");

        assertNotNull(resultado);
        assertTrue(notificacionMock.getLeida());
        verify(notificacionRepository).save(notificacionMock);
    }

    @Test
    @DisplayName("obtenerDetalle: Notificacion no encontrada o no pertenece al usuario lanza excepcion (BOLA/IDOR)")
    void obtenerDetalle_NoExisteONoEsDueno_LanzaResourceNotFoundException() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.findByIdAndUsuario(999L, usuarioMock)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificacionService.obtenerDetalle(999L, "juan@example.com"));
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarComoLeida: Actualiza estado a leida")
    void marcarComoLeida_Exitoso() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.findByIdAndUsuario(100L, usuarioMock)).thenReturn(Optional.of(notificacionMock));

        notificacionService.marcarComoLeida(100L, "juan@example.com");

        assertTrue(notificacionMock.getLeida());
        verify(notificacionRepository).save(notificacionMock);
    }

    @Test
    @DisplayName("marcarTodasComoLeidas: Ejecuta actualizacion masiva en repositorio")
    void marcarTodasComoLeidas_Exitoso() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));

        notificacionService.marcarTodasComoLeidas("juan@example.com");

        verify(notificacionRepository).marcarTodasComoLeidas(usuarioMock);
    }

    @Test
    @DisplayName("contarNoLeidas: Retorna conteo de pendientes de lectura")
    void contarNoLeidas_Exitoso() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));
        when(notificacionRepository.countByUsuarioAndLeidaFalse(usuarioMock)).thenReturn(5L);

        long pendientes = notificacionService.contarNoLeidas("juan@example.com");

        assertEquals(5L, pendientes);
    }

    @Test
    @DisplayName("notificar: Persiste correctamente la notificacion con todos sus metadatos")
    void notificar_Exitoso() {
        Usuario lider = Usuario.builder()
                .id(2L)
                .nombre("Lider Comunitario")
                .rol(Rol.ROLE_LIDER)
                .build();

        notificacionService.notificar(
                usuarioMock,
                alertaMock,
                "Alerta Direccionada",
                "Su alerta ha cambiado a estado DIRECCIONADA",
                EstadoAlerta.DIRECCIONADA,
                "Se asigno a comite",
                lider
        );

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        Notificacion guardada = captor.getValue();

        assertEquals(usuarioMock, guardada.getUsuario());
        assertEquals(alertaMock, guardada.getAlerta());
        assertEquals(10L, guardada.getAlertaId());
        assertEquals("Alerta Direccionada", guardada.getTitulo());
        assertEquals(EstadoAlerta.DIRECCIONADA, guardada.getNuevoEstado());
        assertEquals("Se asigno a comite", guardada.getMotivoAccion());
        assertEquals("Lider Comunitario", guardada.getResponsableNombre());
        assertEquals("ROLE_LIDER", guardada.getResponsableRol());
        assertFalse(guardada.getLeida());
    }

    @Test
    @DisplayName("notificar: Si destinatario es nulo no intenta guardar en base de datos")
    void notificar_DestinatarioNulo_NoGuarda() {
        notificacionService.notificar(
                null,
                alertaMock,
                "Titulo",
                "Mensaje",
                EstadoAlerta.EN_GESTION,
                "Motivo",
                usuarioMock
        );

        verify(notificacionRepository, never()).save(any());
    }
}

