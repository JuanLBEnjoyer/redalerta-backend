package redalerta_backend.modules.notificacion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.notificacion.dto.NotificacionResponseDto;
import redalerta_backend.modules.notificacion.entity.Notificacion;
import redalerta_backend.modules.notificacion.mapper.NotificacionMapper;
import redalerta_backend.modules.notificacion.repository.NotificacionRepository;
import redalerta_backend.modules.notificacion.service.NotificacionService;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;

/**
 * Implementacion del servicio de bandeja de notificaciones.
 * Aplica aislamiento de acceso por usuario (BOLA/IDOR) y transaccionalidad
 * atomica.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionMapper notificacionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionResponseDto> listarNotificaciones(String emailUsuario, Boolean soloNoLeidas,
            Pageable pageable) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        Page<Notificacion> pagina;

        if (Boolean.TRUE.equals(soloNoLeidas)) {
            pagina = notificacionRepository.findByUsuarioAndLeidaOrderByFechaCreacionDesc(usuario, false, pageable);
        } else {
            pagina = notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario, pageable);
        }

        return pagina.map(notificacionMapper::toResponseDto);
    }

    @Override
    @Transactional
    public NotificacionResponseDto obtenerDetalle(Long id, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        Notificacion notificacion = notificacionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Notificación no encontrada o no pertenece al usuario"));

        if (Boolean.FALSE.equals(notificacion.getLeida())) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }

        return notificacionMapper.toResponseDto(notificacion);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long id, String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        Notificacion notificacion = notificacionRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Notificación no encontrada o no pertenece al usuario"));

        if (Boolean.FALSE.equals(notificacion.getLeida())) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        notificacionRepository.marcarTodasComoLeidas(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(String emailUsuario) {
        Usuario usuario = obtenerUsuarioPorEmail(emailUsuario);
        return notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
    }

    @Override
    @Transactional
    public void notificar(
            Usuario destinatario,
            Alerta alerta,
            String titulo,
            String mensaje,
            EstadoAlerta nuevoEstado,
            String motivoAccion,
            Usuario responsable) {
        if (destinatario == null) {
            log.warn("Se intento emitir una notificacion con destinatario nulo para la alerta {}",
                    alerta != null ? alerta.getId() : "desconocida");
            return;
        }

        String respNombre = (responsable != null && responsable.getNombre() != null)
                ? responsable.getNombre()
                : "Sistema Comunitario";
        String respRol = (responsable != null && responsable.getRol() != null)
                ? responsable.getRol().name()
                : "SISTEMA";

        Notificacion notificacion = Notificacion.builder()
                .usuario(destinatario)
                .alerta(alerta)
                .alertaId(alerta != null ? alerta.getId() : 0L)
                .titulo(titulo)
                .mensaje(mensaje)
                .nuevoEstado(nuevoEstado)
                .motivoAccion(motivoAccion)
                .responsableNombre(respNombre)
                .responsableRol(respRol)
                .leida(false)
                .build();

        notificacionRepository.save(notificacion);
        log.info("Notificacion creada exitosamente para usuario '{}' sobre alerta ID {}",
                destinatario.getEmail(), notificacion.getAlertaId());
    }

    private Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
