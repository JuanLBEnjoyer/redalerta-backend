package redalerta_backend.modules.notificacion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.notificacion.dto.NotificacionResponseDto;
import redalerta_backend.modules.usuario.entity.Usuario;

/**
 * Contrato de servicio para la gestion de la bandeja de notificaciones en la aplicacion.
 */
public interface NotificacionService {

    /**
     * Obtiene el listado paginado de notificaciones de un usuario con filtro opcional de no leidas.
     */
    Page<NotificacionResponseDto> listarNotificaciones(String emailUsuario, Boolean soloNoLeidas, Pageable pageable);

    /**
     * Consulta el detalle explicativo de una notificacion y la marca automaticamente como leida.
     */
    NotificacionResponseDto obtenerDetalle(Long id, String emailUsuario);

    /**
     * Marca manualmente una notificacion como leida.
     */
    void marcarComoLeida(Long id, String emailUsuario);

    /**
     * Marca todas las notificaciones pendientes del usuario como leidas.
     */
    void marcarTodasComoLeidas(String emailUsuario);

    /**
     * Retorna la cantidad de notificaciones sin leer del usuario para actualizar badges de UI.
     */
    long contarNoLeidas(String emailUsuario);

    /**
     * Persiste una nueva notificacion dirigida a un usuario destinatario especifico.
     */
    void notificar(
            Usuario destinatario,
            Alerta alerta,
            String titulo,
            String mensaje,
            EstadoAlerta nuevoEstado,
            String motivoAccion,
            Usuario responsable
    );
}

