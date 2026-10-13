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

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

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
