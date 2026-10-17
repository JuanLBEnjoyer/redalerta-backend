package redalerta_backend.modules.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redalerta_backend.modules.admin.dto.AprobarSolicitudRequestDto;
import redalerta_backend.modules.admin.dto.SolicitudRegistroResponseDto;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * Contrato de servicio para la gestion administrativa y auditoria de solicitudes de registro (Vetting).
 */
public interface AdminSolicitudService {

    /**
     * Consulta el listado paginado de solicitudes de registro en la bandeja administrativa.
     * Por defecto retorna las solicitudes en estado PENDIENTE, permitiendo filtrar opcionalmente por rol.
     *
     * @param estado Estado de la solicitud (por defecto PENDIENTE si es nulo).
     * @param rol Filtro opcional por rol institucional o comunitario (ROLE_LIDER o ROLE_ORGANIZACION).
     * @param emailAdmin Correo del administrador autenticado obtenido del token JWT.
     * @param pageable Configuracion de paginacion y ordenamiento.
     * @return Pagina de solicitudes de registro coincidentes.
     */
    Page<SolicitudRegistroResponseDto> listarSolicitudes(EstadoSolicitud estado, Rol rol, String emailAdmin, Pageable pageable);

    /**
     * Consulta el detalle y evidencias de una solicitud de registro especifica por su identificador.
     *
     * @param id Identificador unico de la solicitud de registro.
     * @param emailAdmin Correo del administrador autenticado obtenido del token JWT.
     * @return DTO con la informacion detallada de la postulacion y sus soportes probatorios.
     */
    SolicitudRegistroResponseDto obtenerSolicitudPorId(Long id, String emailAdmin);

    /**
     * Aprueba una postulacion de registro en estado PENDIENTE, activando la cuenta del usuario
     * asociado y registrando la auditoria del administrador que realiza la aprobacion.
     *
     * @param id Identificador unico de la solicitud.
     * @param request DTO con observaciones opcionales del administrador.
     * @param emailAdmin Correo del administrador autenticado obtenido del token JWT.
     * @return DTO con la solicitud actualizada en estado APROBADA y la cuenta de usuario activada.
     */
    SolicitudRegistroResponseDto aprobarSolicitud(Long id, AprobarSolicitudRequestDto request, String emailAdmin);
}
