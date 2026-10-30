package redalerta_backend.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa la informacion resumida y de bandeja para una solicitud de registro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de resumen de una solicitud de registro en la bandeja administrativa")
public class SolicitudRegistroResponseDto {

    @Schema(description = "Identificador unico de la solicitud", example = "1")
    private Long id;

    @Schema(description = "Identificador del usuario postulante", example = "10")
    private Long usuarioId;

    @Schema(description = "Nombre completo del postulante o representante", example = "Carlos Gomez")
    private String nombre;

    @Schema(description = "Correo electronico del usuario", example = "lider@comunidad.org")
    private String email;

    @Schema(description = "Telefono de contacto", example = "3001234567")
    private String telefono;

    @Schema(description = "Rol solicitado para la activacion", example = "ROLE_LIDER")
    private Rol rol;

    @Schema(description = "Numero de documento de identidad o identificacion", example = "1094857213")
    private String documentoNumero;

    @Schema(description = "Tipo de documento de identidad", example = "CEDULA")
    private TipoDeDocumento documentoTipo;

    @Schema(description = "Departamento territorial", example = "Quindio")
    private String departamento;

    @Schema(description = "Municipio territorial", example = "Armenia")
    private String municipio;

    @Schema(description = "Direccion detallada de despacho o residencia", example = "Carrera 14 # 20-30")
    private String direccion;

    @Schema(description = "Sector o comunidad representada (en caso de lider)", example = "Barrio La Castellana - JAC")
    private String sectorComunidad;

    @Schema(description = "Nombre o razon social del comite o brigada comunitaria", example = "Brigada Comunitaria de Emergencias")
    private String razonSocial;

    @Schema(description = "NIT institucional (en caso de organizacion)", example = "900123456-1")
    private String nit;

    @Schema(description = "Estado actual de la solicitud", example = "PENDIENTE")
    private EstadoSolicitud estado;

    @Schema(description = "Observaciones o justificacion del administrador", example = "Documentacion pendiente de revision")
    private String observacionesAdmin;

    @Schema(description = "Fecha y hora en que se envio la postulacion")
    private LocalDateTime fechaPostulacion;

    @Schema(description = "Fecha y hora en que fue evaluada por el administrador")
    private LocalDateTime fechaRevision;

    @Schema(description = "Nombre del administrador que reviso la solicitud")
    private String revisadoPorNombre;

    @Schema(description = "Cantidad de evidencias o soportes adjuntos", example = "2")
    private Integer totalEvidencias;

    @Schema(description = "Listado de soportes probatorios adjuntos")
    private List<EvidenciaSolicitudResponseDto> evidencias;
}
