package redalerta_backend.modules.auth.service;

import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

/**
 * Contrato de servicio para la gestion de autenticacion, registro de usuarios y control de sesiones.
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en la base de datos con rol ROLE_CIUDADANO y vincula su encuesta
     * de caracterizacion sociodemografica en una transaccion atomica.
     *
     * @param request Datos de registro del usuario y encuesta de caracterizacion.
     * @return DTO con la informacion publica del usuario creado.
     * @throws redalerta_backend.common.exception.EmailAlreadyExistException Si el correo electronico ya esta registrado.
     * @throws redalerta_backend.common.exception.BadRequestException         Si el documento de identidad ya esta registrado.
     */
    UsuarioResponseDto registrar(RegistroRequestDto request);

    /**
     * Valida las credenciales de acceso del usuario y emite un token de acceso JWT Bearer.
     * Aplica proteccion contra enumeracion de usuarios retornando mensajes genericos ante fallos.
     *
     * @param request DTO con el correo electronico y la contrasena en texto plano.
     * @return DTO con el token JWT firmado, tipo Bearer y datos del usuario.
     * @throws redalerta_backend.common.exception.UnauthorizedException Si las credenciales no coinciden o la cuenta esta inactiva.
     */
    LoginResponseDto login(LoginRequestDto request);
}
