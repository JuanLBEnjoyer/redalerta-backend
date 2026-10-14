package redalerta_backend.modules.usuario.service;

import redalerta_backend.modules.usuario.dto.ActualizarPerfilRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

/**
 * Contrato de servicio para la gestion de perfiles y usuarios.
 */
public interface UsuarioService {

    /**
     * Consulta la informacion publica y encuesta de caracterizacion del usuario autenticado.
     *
     * @param email Correo electronico extraido de la sesion autenticada.
     * @return DTO con los datos del perfil y encuesta asociada.
     * @throws redalerta_backend.common.exception.ResourceNotFoundException Si el usuario no existe.
     * @throws redalerta_backend.common.exception.UnauthorizedException   Si la cuenta se encuentra inactiva.
     */
    UsuarioResponseDto obtenerPerfil(String email);

    /**
     * Actualiza los datos permitidos del perfil (nombre, email, telefono y contrasena)
     * del usuario autenticado de forma segura.
     *
     * @param email   Correo electronico actual del usuario en sesion.
     * @param request Datos a modificar.
     * @return DTO con la informacion actualizada del usuario.
     * @throws redalerta_backend.common.exception.EmailAlreadyExistException Si el nuevo email ya esta registrado por otro usuario.
     * @throws redalerta_backend.common.exception.BadRequestException         Si la contrasena actual es incorrecta o faltante al solicitar cambio de clave.
     */
    UsuarioResponseDto actualizarPerfil(String email, ActualizarPerfilRequestDto request);
}
