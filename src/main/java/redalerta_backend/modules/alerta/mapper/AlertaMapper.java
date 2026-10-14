package redalerta_backend.modules.alerta.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.EvidenciaResponseDto;
import redalerta_backend.modules.alerta.dto.UbicacionAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;

import java.util.Collections;
import java.util.List;

/**
 * Mapper para transformar entre la entidad Alerta y sus DTOs asociados a la creacion.
 */
@Component
public class AlertaMapper {

    public AlertaResponseDto toResponseDto(Alerta alerta) {
        if (alerta == null) {
            return null;
        }

        return AlertaResponseDto.builder()
                .id(alerta.getId())
                .descripcion(alerta.getDescripcion())
                .ubicacion(toUbicacionAlertaDto(alerta.getUbicacion()))
                .autorId(alerta.getAutor() != null ? alerta.getAutor().getId() : null)
                .autorNombre(alerta.getAutor() != null ? alerta.getAutor().getNombre() : null)
                .estado(alerta.getEstado())
                .evidencias(toEvidenciaResponseDtoList(alerta.getEvidencias()))
                .fechaCreacion(alerta.getFechaCreacion())
                .build();
    }

    public UbicacionAlertaDto toUbicacionAlertaDto(UbicacionAlerta ubicacion) {
        if (ubicacion == null) {
            return null;
        }

        return UbicacionAlertaDto.builder()
                .departamento(ubicacion.departamento())
                .municipio(ubicacion.municipio())
                .direccionDetallada(ubicacion.direccionDetallada())
                .build();
    }

    public UbicacionAlerta toUbicacionAlerta(UbicacionAlertaDto dto) {
        if (dto == null) {
            return null;
        }

        return new UbicacionAlerta(
                dto.getDepartamento(),
                dto.getMunicipio(),
                dto.getDireccionDetallada()
        );
    }

    public List<EvidenciaResponseDto> toEvidenciaResponseDtoList(List<EvidenciaAlerta> evidencias) {
        if (evidencias == null || evidencias.isEmpty()) {
            return Collections.emptyList();
        }

        return evidencias.stream()
                .map(this::toEvidenciaResponseDto)
                .toList();
    }

    public EvidenciaResponseDto toEvidenciaResponseDto(EvidenciaAlerta evidencia) {
        if (evidencia == null) {
            return null;
        }

        return EvidenciaResponseDto.builder()
                .id(evidencia.getId())
                .url(evidencia.getUrl())
                .tipoMime(evidencia.getTipoMime())
                .fechaSubida(evidencia.getFechaSubida())
                .build();
    }
}
