package redalerta_backend.modules.notificacion.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.notificacion.dto.NotificacionResponseDto;
import redalerta_backend.modules.notificacion.entity.Notificacion;

/**
 * Mapeador de transformacion entre entidades Notificacion y DTOs publicos.
 */
@Component
public class NotificacionMapper {

    public NotificacionResponseDto toResponseDto(Notificacion notificacion) {
        if (notificacion == null) {
            return null;
        }
        return NotificacionResponseDto.builder()
                .id(notificacion.getId())
                .alertaId(notificacion.getAlertaId())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .nuevoEstado(notificacion.getNuevoEstado())
                .motivoAccion(notificacion.getMotivoAccion())
                .responsableNombre(notificacion.getResponsableNombre())
                .responsableRol(notificacion.getResponsableRol())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .build();
    }
}

