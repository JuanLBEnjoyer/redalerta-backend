package redalerta_backend.modules.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.EmailAlreadyExistException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.common.mail.EmailService;
import redalerta_backend.config.security.JwtService;
import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.dto.RestablecerPasswordRequestDto;
import redalerta_backend.modules.auth.dto.SolicitarRecuperacionRequestDto;
import redalerta_backend.modules.auth.dto.UsuarioSesionDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoRequestDto;
import redalerta_backend.modules.auth.dto.VerificarCodigoResponseDto;
import redalerta_backend.modules.auth.entity.CodigoRecuperacion;
import redalerta_backend.modules.auth.repository.CodigoRecuperacionRepository;
import redalerta_backend.modules.auth.service.AuthService;
import redalerta_backend.modules.usuario.dto.CaracterizacionRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.CaracterizacionUsuarioRepository;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Genero;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del servicio de autenticacion y registro de usuarios.
 * Aplica principios de Domain-Driven Design (DDD) con Value Objects inmutables,
 * persistencia desacoplada y seguridad defensiva contra enumeracion de cuentas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final CaracterizacionUsuarioRepository caracterizacionRepository;
    private final CodigoRecuperacionRepository codigoRecuperacionRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final int MAX_INTENTOS_OTP = 3;

    /**
     * {@inheritDoc}
     * Persiste el usuario y su caracterizacion social de forma desacoplada
     * dentro de una transaccion atomica.
     */
    @Override
    @Transactional
    public UsuarioResponseDto registrar(RegistroRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistException(email);
        }

        Documento documentoVO = new Documento(
                request.getDocumento().getNumero(),
                request.getDocumento().getTipo()
        );
        if (usuarioRepository.existsByDocumento(documentoVO)) {
            throw new BadRequestException("El documento de identidad ya se encuentra registrado");
        }

        Ubicacion ubicacionVO = usuarioMapper.toUbicacion(request.getUbicacion());

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .documento(documentoVO)
                .ubicacion(ubicacionVO)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        CaracterizacionRequestDto caracDto = request.getCaracterizacion();
        boolean gestacionLactancia = caracDto.getGenero() == Genero.FEMENINO
                && Boolean.TRUE.equals(caracDto.getEnGestacionLactancia());

        CaracterizacionUsuario caracterizacion = CaracterizacionUsuario.builder()
                .usuario(usuarioGuardado)
                .fechaNacimiento(caracDto.getFechaNacimiento())
                .genero(caracDto.getGenero())
                .enGestacionLactancia(gestacionLactancia)
                .numeroIntegrantesFamilia(caracDto.getNumeroIntegrantesFamilia())
                .tieneMenoresDeEdad(Boolean.TRUE.equals(caracDto.getTieneMenoresDeEdad()))
                .cantidadMenores(Boolean.TRUE.equals(caracDto.getTieneMenoresDeEdad()) && caracDto.getCantidadMenores() != null
                        ? caracDto.getCantidadMenores() : 0)
                .tieneMiembroDiscapacidad(Boolean.TRUE.equals(caracDto.getTieneMiembroDiscapacidad()))
                .especificacionDiscapacidad(Boolean.TRUE.equals(caracDto.getTieneMiembroDiscapacidad())
                        ? caracDto.getEspecificacionDiscapacidad() : null)
                .perteneceGrupoEtnico(Boolean.TRUE.equals(caracDto.getPerteneceGrupoEtnico()))
                .especificacionGrupoEtnico(Boolean.TRUE.equals(caracDto.getPerteneceGrupoEtnico())
                        ? caracDto.getEspecificacionGrupoEtnico() : null)
                .build();

        CaracterizacionUsuario caracterizacionGuardada = caracterizacionRepository.save(caracterizacion);

        return usuarioMapper.toResponseDto(usuarioGuardado, caracterizacionGuardada);
    }

    /**
     * {@inheritDoc}
     * Ejecuta una consulta de solo lectura, verifica el hash de la contrasena con BCrypt
     * y genera el token JWT con los claims del usuario autenticado.
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("La cuenta de usuario se encuentra inactiva");
        }

        String token = jwtService.generateToken(usuario);
        UsuarioSesionDto usuarioSesionDto = usuarioMapper.toSesionDto(usuario);

        return LoginResponseDto.builder()
                .token(token)
                .tipo("Bearer")
                .usuario(usuarioSesionDto)
                .build();
    }

    /**
     * {@inheritDoc}
     * Limpia el SecurityContextHolder para el hilo de solicitud actual.
     */
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * {@inheritDoc}
     * Aplica mitigacion contra enumeracion de cuentas (OWASP): si el usuario no existe o se encuentra inactivo,
     * no se arroja excepcion ni se da pista alguna sobre la presencia del correo en la base de datos.
     * Invalida los codigos previos y genera un nuevo codigo numerico de 6 digitos criptograficamente seguro.
     */
    @Override
    @Transactional
    public void solicitarRecuperacionPassword(SolicitarRecuperacionRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            log.info("Solicitud de recuperacion de contrasena para correo no registrado: {}", email);
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            log.info("Solicitud de recuperacion de contrasena para cuenta inactiva: {}", email);
            return;
        }

        List<CodigoRecuperacion> codigosActivosPrevios = codigoRecuperacionRepository.findByUsuarioAndUsadoFalse(usuario);
        for (CodigoRecuperacion codigoPrevio : codigosActivosPrevios) {
            codigoPrevio.setUsado(true);
        }
        codigoRecuperacionRepository.saveAll(codigosActivosPrevios);

        int numeroOtp = 100000 + secureRandom.nextInt(900000);
        String codigoOtp = String.valueOf(numeroOtp);

        CodigoRecuperacion nuevoCodigo = CodigoRecuperacion.builder()
                .usuario(usuario)
                .codigoHash(passwordEncoder.encode(codigoOtp))
                .fechaExpiracion(LocalDateTime.now().plusMinutes(10))
                .usado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        codigoRecuperacionRepository.save(nuevoCodigo);

        emailService.enviarCodigoRecuperacion(usuario.getEmail(), usuario.getNombre(), codigoOtp);
    }

    /**
     * {@inheritDoc}
     * Valida de forma intermedia el codigo OTP de 6 digitos.
     * Implementa proteccion estricta contra fuerza bruta invalidando el codigo al superar 3 intentos fallidos.
     * Al validar satisfactoriamente, invalida el OTP y emite un token temporal firmado de restablecimiento (resetToken).
     */
    @Override
    @Transactional
    public VerificarCodigoResponseDto verificarCodigoRecuperacion(VerificarCodigoRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("El codigo de recuperacion es invalido o ha expirado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva");
        }

        CodigoRecuperacion codigoRecuperacion = codigoRecuperacionRepository
                .findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(usuario)
                .orElseThrow(() -> new BadRequestException("El codigo de recuperacion es invalido o ha expirado"));

        if (codigoRecuperacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            codigoRecuperacion.setUsado(true);
            codigoRecuperacionRepository.save(codigoRecuperacion);
            throw new BadRequestException("El codigo de recuperacion ha expirado. Por favor, solicite uno nuevo");
        }

        if (codigoRecuperacion.getIntentosFallidos() >= MAX_INTENTOS_OTP) {
            codigoRecuperacion.setUsado(true);
            codigoRecuperacionRepository.save(codigoRecuperacion);
            throw new BadRequestException("Ha superado el numero maximo de intentos permitidos. Por seguridad, solicite un nuevo codigo");
        }

        if (!passwordEncoder.matches(request.getCodigo(), codigoRecuperacion.getCodigoHash())) {
            int nuevosIntentos = codigoRecuperacion.getIntentosFallidos() + 1;
            codigoRecuperacion.setIntentosFallidos(nuevosIntentos);
            if (nuevosIntentos >= MAX_INTENTOS_OTP) {
                codigoRecuperacion.setUsado(true);
                codigoRecuperacionRepository.save(codigoRecuperacion);
                throw new BadRequestException("Ha superado el numero maximo de intentos permitidos. Por seguridad, solicite un nuevo codigo");
            }
            codigoRecuperacionRepository.save(codigoRecuperacion);
            int intentosRestantes = MAX_INTENTOS_OTP - nuevosIntentos;
            throw new BadRequestException("El codigo de recuperacion ingresado no es correcto. Intentos restantes: " + intentosRestantes);
        }

        // Marcar codigo como consumido de inmediato para evitar replay attacks
        codigoRecuperacion.setUsado(true);
        codigoRecuperacionRepository.save(codigoRecuperacion);

        String resetToken = jwtService.generatePasswordResetToken(usuario.getEmail());

        return VerificarCodigoResponseDto.builder()
                .resetToken(resetToken)
                .mensaje("Codigo verificado exitosamente")
                .build();
    }

    /**
     * {@inheritDoc}
     * Valida el token temporal de restablecimiento (resetToken) y actualiza la contrasena del usuario.
     */
    @Override
    @Transactional
    public void restablecerPassword(RestablecerPasswordRequestDto request) {
        if (!jwtService.isPasswordResetTokenValid(request.getResetToken())) {
            throw new BadRequestException("El token de restablecimiento es invalido o ha expirado. Por favor, inicie el proceso nuevamente");
        }

        String email = jwtService.extractUsername(request.getResetToken());
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("El usuario asociado al token no fue encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }
}
