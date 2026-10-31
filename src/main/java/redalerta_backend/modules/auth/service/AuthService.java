package redalerta_backend.modules.auth.service;

import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.dto.RestablecerPasswordRequestDto;
import redalerta_backend.modules.auth.dto.SolicitarRecuperacionRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoResponseDto;
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

    /**
     * Invalida el contexto de seguridad de la sesion del usuario autenticado en el hilo de ejecucion actual.
     */
    void logout();

    /**
     * Procesa la solicitud de recuperacion de contrasena emitiendo un codigo OTP de 6 digitos al correo del usuario.
     * Implementa proteccion contra enumeracion de usuarios (OWASP) retornando de forma transparente
     * sin revelar si la cuenta existe o no en el sistema.
     *
     * @param request DTO con el correo electronico de la cuenta a recuperar.
     */
    void solicitarRecuperacionPassword(SolicitarRecuperacionRequestDto request);

    /**
     * Valida de manera intermedia el codigo OTP de 6 digitos ingresado por el usuario.
     * Aplica proteccion contra fuerza bruta limitando los intentos fallidos a un maximo de 3.
     * Si el codigo es valido, lo invalida de inmediato y emite un token temporal de restablecimiento (resetToken).
     *
     * @param request DTO con el correo electronico y el codigo OTP recibido.
     * @return DTO con el resetToken firmado y mensaje de exito.
     * @throws redalerta_backend.common.exception.BadRequestException Si el codigo es invalido, expiro o supero intentos maximos.
     */
    VerificarCodigoResponseDto verificarCodigoRecuperacion(VerificarCodigoRequestDto request);

    /**
     * Valida el token temporal de restablecimiento (resetToken) y actualiza la contrasena del usuario.
     *
     * @param request DTO con el resetToken firmado y la nueva contrasena.
     * @throws redalerta_backend.common.exception.BadRequestException Si el token es invalido, expiro o la cuenta esta inactiva.
     */
    void restablecerPassword(RestablecerPasswordRequestDto request);
}
