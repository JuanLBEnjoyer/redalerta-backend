package redalerta_backend.modules.alerta.service;

import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;

/**
 * Contrato de servicio para la creacion de alertas comunitarias.
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
}
