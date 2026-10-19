package redalerta_backend.modules.alerta.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;

/**
 * Contrato de servicio para la creacion y consulta de alertas comunitarias.
 */
public interface AlertaService {

    /**
     * Registra una nueva alerta comunitaria asociada al ciudadano autenticado.
     *
     * @param request      Datos de la alerta reportada por el ciudadano.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @return Alerta registrada con su estado inicial.
     */
    AlertaResponseDto crearAlerta(CrearAlertaRequestDto request, String emailUsuario);

    /**
     * Retorna el listado paginado de alertas creadas exclusivamente por el
     * ciudadano autenticado.
     *
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @param pageable     Configuracion de paginacion y ordenamiento.
     * @return Pagina de alertas del ciudadano.
     */
    Page<AlertaResponseDto> listarMisAlertas(String emailUsuario, Pageable pageable);

    /**
     * Retorna el listado paginado de alertas comunitarias aplicando los filtros
     * especificados.
     * El filtro por departamento es un valor fijo e inmutable determinado por la
     * ubicacion territorial del usuario autenticado. El filtrado por municipio es opcional,
     * permitiendo consultar todas las alertas del departamento o las de un municipio puntual.
     * Si no se especifica un estado puntual, retorna por defecto las alertas en
     * estados activos (PENDIENTE_REVISION, DIRECCIONADA, EN_GESTION).
     *
     * @param filtro       Criterios de filtrado (municipio opcional, estado, rango de fechas).
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @param pageable     Configuracion de paginacion y ordenamiento.
     * @return Pagina de alertas que cumplen con los criterios de busqueda.
     */
    Page<AlertaResponseDto> listarAlertas(FiltroAlertaDto filtro, String emailUsuario, Pageable pageable);

    /**
     * Obtiene el detalle exhaustivo de una alerta comunitaria por su identificador
     * unico.
     *
     * @param id           Identificador unico de la alerta.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @return Detalle de la alerta encontrada en formato DTO.
     */
    AlertaResponseDto obtenerAlertaPorId(Long id, String emailUsuario);

    /**
     * Elimina una alerta comunitaria creada por el ciudadano autenticado.
     * Unicamente se permite la eliminacion si la alerta se encuentra en estado
     * PENDIENTE_REVISION.
     *
     * @param id           Identificador unico de la alerta a eliminar.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     */
    void eliminarAlerta(Long id, String emailUsuario);

    /**
     * Modifica una alerta comunitaria creada por el ciudadano autenticado.
     * Unicamente se permite la modificacion si la alerta se encuentra en estado
     * PENDIENTE_REVISION.
     *
     * @param id           Identificador unico de la alerta a modificar.
     * @param request      Datos actualizados de la alerta (descripcion, ubicacion,
     *                     evidencias).
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @return Alerta actualizada en su representacion DTO.
     */
    AlertaResponseDto modificarAlerta(Long id, ModificarAlertaRequestDto request, String emailUsuario);

    /**
     * Valida, comprime y sube una evidencia fotografica a AWS S3 para el ciudadano autenticado.
     *
     * @param archivo      Archivo binario de imagen multipart enviado por el cliente.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @return DTO con la URL y metadatos de la evidencia almacenada en la nube.
     */
    redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto subirEvidencia(
            org.springframework.web.multipart.MultipartFile archivo, String emailUsuario);

    /**
     * Consulta las organizaciones de socorro o gubernamentales elegibles en la jurisdiccion del lider.
     *
     * @param emailLider Correo del lider comunitario autenticado.
     * @return Lista de organizaciones activas en la jurisdiccion territorial.
     */
    java.util.List<redalerta_backend.modules.alerta.dto.OrganizacionResponseDto> listarOrganizacionesElegibles(String emailLider);

    /**
     * Ejecuta el triaje y direccionamiento formal de una alerta hacia una organizacion competente.
     * Asigna categoria, prioridad, observacion tecnica y cambia el estado a DIRECCIONADA.
     *
     * @param alertaId   Identificador unico de la alerta.
     * @param request    Datos del triaje (categoria, prioridad, organizacionId, observacionTecnica).
     * @param emailLider Correo del lider comunitario autenticado.
     * @return Alerta direccionada con historial actualizado.
     */
    AlertaResponseDto direccionarAlerta(
            Long alertaId,
            redalerta_backend.modules.alerta.dto.DireccionarAlertaRequestDto request,
            String emailLider
    );

    /**
     * Consulta el listado de alertas que han sido direccionadas por el lider comunitario autenticado.
     *
     * @param emailLider Correo del lider comunitario autenticado.
     * @param pageable   Configuracion de paginacion.
     * @return Pagina de alertas direccionadas por el lider.
     */
    Page<AlertaResponseDto> listarAlertasDireccionadasPorLider(String emailLider, Pageable pageable);

    /**
     * Consulta la bandeja de entrada de alertas asignadas a la organizacion autenticada.
     *
     * @param emailOrganizacion Correo de la organizacion autenticada.
     * @param pageable          Configuracion de paginacion.
     * @return Pagina de alertas en bandeja de atencion de la entidad.
     */
    Page<AlertaResponseDto> listarBandejaOrganizacion(String emailOrganizacion, Pageable pageable);

    /**
     * Modifica el direccionamiento y triaje de una alerta comunitaria en estado DIRECCIONADA.
     * Permite al lider autor o administrador corregir la categoria, prioridad, observacion o cambiar la organizacion receptora.
     *
     * @param alertaId   Identificador unico de la alerta.
     * @param request    Nuevos datos del triaje (categoria, prioridad, organizacionId, observacionTecnica).
     * @param emailLider Correo del lider comunitario autenticado.
     * @return Alerta actualizada en su representacion DTO.
     */
    AlertaResponseDto modificarDireccionamiento(
            Long alertaId,
            redalerta_backend.modules.alerta.dto.DireccionarAlertaRequestDto request,
            String emailLider
    );

    /**
     * Cancela o revierte el direccionamiento de una alerta en estado DIRECCIONADA.
     * Retorna la alerta a estado PENDIENTE_REVISION, desvincula la entidad y el lider asignado,
     * y registra la justificacion formal en el historial de auditoria.
     *
     * @param alertaId   Identificador unico de la alerta.
     * @param request    Justificacion formal de la cancelacion.
     * @param emailLider Correo del lider comunitario autenticado.
     * @return Alerta retornada a estado PENDIENTE_REVISION.
     */
    AlertaResponseDto cancelarDireccionamiento(
            Long alertaId,
            redalerta_backend.modules.alerta.dto.CancelarDireccionamientoRequestDto request,
            String emailLider
    );
}
