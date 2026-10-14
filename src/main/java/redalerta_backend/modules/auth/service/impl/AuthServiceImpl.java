package redalerta_backend.modules.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.EmailAlreadyExistException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.config.security.JwtService;
import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.service.AuthService;
import redalerta_backend.modules.usuario.dto.CaracterizacionRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Genero;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * Implementacion del servicio de autenticacion y registro de usuarios.
 * Aplica principios de Domain-Driven Design (DDD) con Value Objects inmutables,
 * persistencia en cascada y seguridad defensiva contra enumeracion de cuentas.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;

    /**
     * {@inheritDoc}
     * Persiste el usuario y su entidad de caracterizacion en cascada (CascadeType.ALL)
     * asegurando atomicidad transaccional.
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

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .documento(documentoVO)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .rol(Rol.ROLE_CIUDADANO)
                .activo(true)
                .build();

        CaracterizacionRequestDto caracDto = request.getCaracterizacion();
        boolean gestacionLactancia = caracDto.getGenero() == Genero.FEMENINO
                && Boolean.TRUE.equals(caracDto.getEnGestacionLactancia());

        CaracterizacionUsuario caracterizacion = CaracterizacionUsuario.builder()
                .fechaNacimiento(caracDto.getFechaNacimiento())
                .genero(caracDto.getGenero())
                .enGestacionLactancia(gestacionLactancia)
                .direccionResidencia(caracDto.getDireccionResidencia().trim())
                .barrio(caracDto.getBarrio().trim())
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

        nuevoUsuario.setCaracterizacion(caracterizacion);

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        return usuarioMapper.toResponseDto(usuarioGuardado);
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
        UsuarioResponseDto usuarioDto = usuarioMapper.toResponseDto(usuario);

        return LoginResponseDto.builder()
                .token(token)
                .tipo("Bearer")
                .usuario(usuarioDto)
                .build();
    }
}
