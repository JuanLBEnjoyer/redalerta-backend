package redalerta_backend.modules.usuario.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.auth.dto.UsuarioSesionDto;
import redalerta_backend.modules.usuario.dto.CaracterizacionResponseDto;
import redalerta_backend.modules.usuario.dto.DocumentoResponseDto;
import redalerta_backend.modules.usuario.dto.UbicacionRequestDto;
import redalerta_backend.modules.usuario.dto.UbicacionResponseDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

@Component
public class UsuarioMapper {

    public UsuarioSesionDto toSesionDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioSesionDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    public UsuarioResponseDto toResponseDto(Usuario usuario) {
        return toResponseDto(usuario, null);
    }

    public UsuarioResponseDto toResponseDto(Usuario usuario, CaracterizacionUsuario caracterizacion) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponseDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .documento(toDocumentoResponseDto(usuario.getDocumento()))
                .ubicacion(toUbicacionResponseDto(usuario.getUbicacion()))
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .fechaRegistro(usuario.getFechaRegistro())
                .caracterizacion(toCaracterizacionResponseDto(caracterizacion))
                .build();
    }

    public DocumentoResponseDto toDocumentoResponseDto(Documento documento) {
        if (documento == null) {
            return null;
        }

        return DocumentoResponseDto.builder()
                .numero(documento.numero())
                .tipo(documento.tipo())
                .build();
    }

    public UbicacionResponseDto toUbicacionResponseDto(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }

        return UbicacionResponseDto.builder()
                .departamento(ubicacion.departamento())
                .municipio(ubicacion.municipio())
                .direccionResidencia(ubicacion.direccionResidencia())
                .build();
    }

    public Ubicacion toUbicacion(UbicacionRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return new Ubicacion(
                dto.getDepartamento(),
                dto.getMunicipio(),
                dto.getDireccionResidencia()
        );
    }

    public CaracterizacionResponseDto toCaracterizacionResponseDto(CaracterizacionUsuario caracterizacion) {
        if (caracterizacion == null) {
            return null;
        }

        return CaracterizacionResponseDto.builder()
                .id(caracterizacion.getId())
                .fechaNacimiento(caracterizacion.getFechaNacimiento())
                .edadCalculada(caracterizacion.getEdadCalculada())
                .genero(caracterizacion.getGenero())
                .enGestacionLactancia(caracterizacion.getEnGestacionLactancia())
                .numeroIntegrantesFamilia(caracterizacion.getNumeroIntegrantesFamilia())
                .tieneMenoresDeEdad(caracterizacion.getTieneMenoresDeEdad())
                .cantidadMenores(caracterizacion.getCantidadMenores())
                .tieneMiembroDiscapacidad(caracterizacion.getTieneMiembroDiscapacidad())
                .especificacionDiscapacidad(caracterizacion.getEspecificacionDiscapacidad())
                .perteneceGrupoEtnico(caracterizacion.getPerteneceGrupoEtnico())
                .especificacionGrupoEtnico(caracterizacion.getEspecificacionGrupoEtnico())
                .fechaEncuesta(caracterizacion.getFechaEncuesta())
                .build();
    }
}
