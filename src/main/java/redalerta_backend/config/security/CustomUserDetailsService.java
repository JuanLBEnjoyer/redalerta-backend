package redalerta_backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Email;

import java.util.Collections;

/**
 * Adaptador personalizado de {@link UserDetailsService} de Spring Security.
 * Recupera la entidad de dominio {@link Usuario} utilizando el Value Object inmutable {@link Email}
 * y la mapea a la representacion de usuario reconocida por el contexto de seguridad.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga el usuario a partir de su direccion de correo electronico.
     *
     * @param emailStr Direccion de correo electronico en formato cadena.
     * @return Instancia de {@link UserDetails} con credenciales, estado activo y rol asignado.
     * @throws UsernameNotFoundException Si no se encuentra un usuario registrado con dicho correo.
     */
    @Override
    public UserDetails loadUserByUsername(String emailStr) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(new Email(emailStr))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + emailStr));

        return new User(
                usuario.getEmail().valor(),
                usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol().name()))
        );
    }
}
