package redalerta_backend.modules.admin.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.admin.dto.EvidenciaSolicitudResponseDto;
import redalerta_backend.modules.admin.dto.SolicitudRegistroResponseDto;
import redalerta_backend.modules.admin.entity.EvidenciaSolicitud;
import redalerta_backend.modules.admin.entity.SolicitudRegistro;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.util.Collections;
import java.util.List;

/**
 * Mapper para transformar entre la entidad {@link SolicitudRegistro} y sus DTOs asociados.
 */
@Component
public class SolicitudRegistroMapper {

    public SolicitudRegistroResponseDto toResponseDto(SolicitudRegistro solicitud) {
        if (solicitud == null) {
            return null;
        }

        Usuario usuario = solicitud.getUsuario();

        return SolicitudRegistroResponseDto.builder()
                .id(solicitud.getId())
                .usuarioId(usuario != null ? usuario.getId() : null)
                .nombre(usuario != null ? usuario.getNombre() : null)
                .email(usuario != null ? usuario.getEmail() : null)
                .telefono(usuario != null ? usuario.getTelefono() : null)
                .rol(usuario != null ? usuario.getRol() : null)
                .documentoNumero(usuario != null && usuario.getDocumento() != null ? usuario.getDocumento().numero() : null)
                .documentoTipo(usuario != null && usuario.getDocumento() != null ? usuario.getDocumento().tipo() : null)
                .departamento(usuario != null && usuario.getUbicacion() != null ? usuario.getUbicacion().departamento() : null)
                .municipio(usuario != null && usuario.getUbicacion() != null ? usuario.getUbicacion().municipio() : null)
                .direccion(usuario != null && usuario.getUbicacion() != null ? usuario.getUbicacion().direccionResidencia() : null)
                .sectorComunidad(solicitud.getSectorComunidad())
                .razonSocial(solicitud.getRazonSocial())
                .nit(solicitud.getNit())
                .estado(solicitud.getEstado())
                .observacionesAdmin(solicitud.getObservacionesAdmin())
                .fechaPostulacion(solicitud.getFechaPostulacion())
                .fechaRevision(solicitud.getFechaRevision())
                .revisadoPorNombre(solicitud.getRevisadoPor() != null ? solicitud.getRevisadoPor().getNombre() : null)
                .totalEvidencias(solicitud.getEvidencias() != null ? solicitud.getEvidencias().size() : 0)
                .evidencias(toEvidenciaResponseDtoList(solicitud.getEvidencias()))
                .build();
    }

    public List<EvidenciaSolicitudResponseDto> toEvidenciaResponseDtoList(List<EvidenciaSolicitud> evidencias) {
        if (evidencias == null || evidencias.isEmpty()) {
            return Collections.emptyList();
        }
        return evidencias.stream()
                .map(this::toEvidenciaResponseDto)
                .toList();
    }

    public EvidenciaSolicitudResponseDto toEvidenciaResponseDto(EvidenciaSolicitud evidencia) {
        if (evidencia == null) {
            return null;
        }
        return EvidenciaSolicitudResponseDto.builder()
                .id(evidencia.getId())
                .url(evidencia.getUrl())
                .nombreArchivo(evidencia.getNombreArchivo())
                .fechaSubida(evidencia.getFechaSubida())
                .build();
    }
}
