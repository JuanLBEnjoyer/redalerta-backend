package redalerta_backend.modules.usuario.mapper;

import org.springframework.stereotype.Component;
import redalerta_backend.modules.usuario.dto.CaracterizacionResponseDto;
import redalerta_backend.modules.usuario.dto.DocumentoResponseDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Documento;

@Component
public class UsuarioMapper {

    public UsuarioResponseDto toResponseDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponseDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .documento(toDocumentoResponseDto(usuario.getDocumento()))
                .email(usuario.getEmail() != null ? usuario.getEmail().valor() : null)
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .fechaRegistro(usuario.getFechaRegistro())
                .caracterizacion(toCaracterizacionResponseDto(usuario.getCaracterizacion()))
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
                .direccionResidencia(caracterizacion.getDireccionResidencia())
                .barrio(caracterizacion.getBarrio())
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
