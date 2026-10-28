package redalerta_backend.modules.usuario.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.EmailAlreadyExistException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.usuario.dto.ActualizarPerfilRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.mapper.UsuarioMapper;
import redalerta_backend.modules.usuario.repository.CaracterizacionUsuarioRepository;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.service.UsuarioService;

/**
 * Implementacion del servicio de gestion de perfiles de usuario.
 * Garantiza reglas de negocio estrictas: inmutabilidad del documento de identidad y rol,
 * validacion de unicidad de correo y verificacion de contrasena previa ante cambios de clave.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CaracterizacionUsuarioRepository caracterizacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerPerfil(String email) {
        Usuario usuario = buscarUsuarioPorEmail(email);
        var caracterizacion = caracterizacionRepository.findByUsuarioId(usuario.getId()).orElse(null);
        return usuarioMapper.toResponseDto(usuario, caracterizacion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UsuarioResponseDto actualizarPerfil(String email, ActualizarPerfilRequestDto request) {
        Usuario usuario = buscarUsuarioPorEmail(email);

        // 1. Validar y actualizar correo electronico si fue modificado
        String nuevoEmail = request.getEmail().trim().toLowerCase();
        if (!nuevoEmail.equalsIgnoreCase(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(nuevoEmail)) {
                throw new EmailAlreadyExistException(nuevoEmail);
            }
            usuario.setEmail(nuevoEmail);
        }

        // 2. Actualizar nombre y telefono
        usuario.setNombre(request.getNombre().trim());
        usuario.setTelefono(request.getTelefono() != null && !request.getTelefono().isBlank()
                ? request.getTelefono().trim()
                : null);

        // 3. Actualizar ubicacion de residencia si fue suministrada
        if (request.getUbicacion() != null) {
            usuario.setUbicacion(usuarioMapper.toUbicacion(request.getUbicacion()));
        }

        // 4. Gestion segura de cambio de contrasena
        if (request.getPasswordNuevo() != null && !request.getPasswordNuevo().isBlank()) {
            if (request.getPasswordActual() == null || request.getPasswordActual().isBlank()) {
                throw new BadRequestException("Debe ingresar su contraseña actual para definir una nueva");
            }
            if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
                throw new BadRequestException("La contraseña actual no es correcta");
            }
            usuario.setPassword(passwordEncoder.encode(request.getPasswordNuevo().trim()));
        }

        // 5. Persistir cambios
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        var caracterizacion = caracterizacionRepository.findByUsuarioId(usuarioActualizado.getId()).orElse(null);
        return usuarioMapper.toResponseDto(usuarioActualizado, caracterizacion);
    }

    private Usuario buscarUsuarioPorEmail(String emailStr) {
        String email = emailStr.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailStr));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("La cuenta de usuario se encuentra inactiva");
        }

        return usuario;
    }
}
