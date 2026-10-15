package redalerta_backend.modules.alerta.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;

/**
 * Contrato de servicio para la creacion y consulta de alertas comunitarias.
 */
public interface AlertaService {

    /**
     * Registra una nueva alerta comunitaria asociada al ciudadano autenticado.
     *
     * @param request Datos de la alerta reportada por el ciudadano.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @return Alerta registrada con su estado inicial.
     */
    AlertaResponseDto crearAlerta(CrearAlertaRequestDto request, String emailUsuario);

    /**
     * Retorna el listado paginado de alertas creadas exclusivamente por el ciudadano autenticado.
     *
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     * @param pageable Configuracion de paginacion y ordenamiento.
     * @return Pagina de alertas del ciudadano.
     */
    Page<AlertaResponseDto> listarMisAlertas(String emailUsuario, Pageable pageable);

    /**
     * Obtiene el detalle de una alerta especifica perteneciente al ciudadano autenticado.
     *
     * @param id Identificador unico de la alerta.
     * @param emailUsuario Correo del usuario autenticado.
     * @return Detalle de la alerta encontrada.
     */
    AlertaResponseDto obtenerAlertaPorId(Long id, String emailUsuario);

    /**
     * Elimina una alerta comunitaria creada por el ciudadano autenticado.
     * Unicamente se permite la eliminacion si la alerta se encuentra en estado PENDIENTE_REVISION.
     *
     * @param id Identificador unico de la alerta a eliminar.
     * @param emailUsuario Correo del usuario autenticado obtenido del token JWT.
     */
    void eliminarAlerta(Long id, String emailUsuario);
}
