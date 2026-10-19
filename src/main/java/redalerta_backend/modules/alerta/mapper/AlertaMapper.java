package redalerta_backend.modules.alerta.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.EvidenciaResponseDto;
import redalerta_backend.modules.alerta.dto.RegistroHistorialResponseDto;
import redalerta_backend.modules.alerta.dto.UbicacionAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.valueobject.RegistroHistorial;
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

        int totalEvidencias = alerta.getEvidencias() != null ? alerta.getEvidencias().size() : 0;
        boolean esAnonimo = Boolean.TRUE.equals(alerta.getEsAnonimo());
        Long autorId = esAnonimo ? null : (alerta.getAutor() != null ? alerta.getAutor().getId() : null);
        String autorNombre = esAnonimo ? "Anónimo" : (alerta.getAutor() != null ? alerta.getAutor().getNombre() : null);

        Long liderId = alerta.getLiderComunitario() != null ? alerta.getLiderComunitario().getId() : null;
        String liderNombre = alerta.getLiderComunitario() != null ? alerta.getLiderComunitario().getNombre() : null;
        Long orgId = alerta.getOrganizacionAsignada() != null ? alerta.getOrganizacionAsignada().getId() : null;
        String orgNombre = alerta.getOrganizacionAsignada() != null ? alerta.getOrganizacionAsignada().getNombre() : null;

        return AlertaResponseDto.builder()
                .id(alerta.getId())
                .descripcion(alerta.getDescripcion())
                .ubicacion(toUbicacionAlertaDto(alerta.getUbicacion()))
                .autorId(autorId)
                .autorNombre(autorNombre)
                .estado(alerta.getEstado())
                .categoria(alerta.getCategoria())
                .prioridad(alerta.getPrioridad())
                .esAnonimo(esAnonimo)
                .totalEvidencias(totalEvidencias)
                .evidencias(toEvidenciaResponseDtoList(alerta.getEvidencias()))
                .historial(toHistorialResponseDtoList(alerta.getHistorial()))
                .liderId(liderId)
                .liderNombre(liderNombre)
                .organizacionId(orgId)
                .organizacionNombre(orgNombre)
                .observacionTecnica(alerta.getObservacionTecnica())
                .fechaCreacion(alerta.getFechaCreacion())
                .build();
    }

    public AlertaResponseDto toResumenResponseDto(Alerta alerta, int maxEvidencias) {
        if (alerta == null) {
            return null;
        }

        int totalEvidencias = alerta.getEvidencias() != null ? alerta.getEvidencias().size() : 0;
        boolean esAnonimo = Boolean.TRUE.equals(alerta.getEsAnonimo());
        Long autorId = esAnonimo ? null : (alerta.getAutor() != null ? alerta.getAutor().getId() : null);
        String autorNombre = esAnonimo ? "Anónimo" : (alerta.getAutor() != null ? alerta.getAutor().getNombre() : null);

        Long liderId = alerta.getLiderComunitario() != null ? alerta.getLiderComunitario().getId() : null;
        String liderNombre = alerta.getLiderComunitario() != null ? alerta.getLiderComunitario().getNombre() : null;
        Long orgId = alerta.getOrganizacionAsignada() != null ? alerta.getOrganizacionAsignada().getId() : null;
        String orgNombre = alerta.getOrganizacionAsignada() != null ? alerta.getOrganizacionAsignada().getNombre() : null;

        List<EvidenciaResponseDto> evidenciasResumen = Collections.emptyList();
        if (alerta.getEvidencias() != null && !alerta.getEvidencias().isEmpty()) {
            evidenciasResumen = alerta.getEvidencias().stream()
                    .limit(maxEvidencias > 0 ? maxEvidencias : totalEvidencias)
                    .map(this::toEvidenciaResponseDto)
                    .toList();
        }

        return AlertaResponseDto.builder()
                .id(alerta.getId())
                .descripcion(alerta.getDescripcion())
                .ubicacion(toUbicacionAlertaDto(alerta.getUbicacion()))
                .autorId(autorId)
                .autorNombre(autorNombre)
                .estado(alerta.getEstado())
                .categoria(alerta.getCategoria())
                .prioridad(alerta.getPrioridad())
                .esAnonimo(esAnonimo)
                .totalEvidencias(totalEvidencias)
                .evidencias(evidenciasResumen)
                .historial(toHistorialResponseDtoList(alerta.getHistorial()))
                .liderId(liderId)
                .liderNombre(liderNombre)
                .organizacionId(orgId)
                .organizacionNombre(orgNombre)
                .observacionTecnica(alerta.getObservacionTecnica())
                .fechaCreacion(alerta.getFechaCreacion())
                .build();
    }

    public List<RegistroHistorialResponseDto> toHistorialResponseDtoList(List<RegistroHistorial> historial) {
        if (historial == null || historial.isEmpty()) {
            return Collections.emptyList();
        }

        return historial.stream()
                .map(this::toHistorialResponseDto)
                .toList();
    }

    public RegistroHistorialResponseDto toHistorialResponseDto(RegistroHistorial registro) {
        if (registro == null) {
            return null;
        }

        return RegistroHistorialResponseDto.builder()
                .motivoOAccion(registro.motivoOAccion())
                .fechaAccion(registro.fechaAccion())
                .estadoResultante(registro.estadoResultante())
                .responsableId(registro.responsableId())
                .responsableNombre(registro.responsableNombre())
                .responsableRol(registro.responsableRol())
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

    public redalerta_backend.modules.alerta.dto.OrganizacionResponseDto toOrganizacionResponseDto(redalerta_backend.modules.usuario.entity.Usuario organizacion) {
        if (organizacion == null) {
            return null;
        }

        String depto = organizacion.getUbicacion() != null ? organizacion.getUbicacion().departamento() : null;
        String muni = organizacion.getUbicacion() != null ? organizacion.getUbicacion().municipio() : null;

        return redalerta_backend.modules.alerta.dto.OrganizacionResponseDto.builder()
                .id(organizacion.getId())
                .nombre(organizacion.getNombre())
                .email(organizacion.getEmail())
                .telefono(organizacion.getTelefono())
                .departamento(depto)
                .municipio(muni)
                .build();
    }
}
