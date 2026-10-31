package redalerta_backend.common.mail;

/**
 * Contrato de servicio para la emision de notificaciones y correos electronicos transaccionales.
 */
public interface EmailService {

    /**
     * Envia el codigo de recuperacion de contrasena de 6 digitos al correo del usuario.
     *
     * @param destinatario  Direccion de correo electronico receptora.
     * @param nombreUsuario Nombre del usuario registrado.
     * @param codigoOtp     Codigo numerico de un solo uso.
     */
    void enviarCodigoRecuperacion(String destinatario, String nombreUsuario, String codigoOtp);
}

