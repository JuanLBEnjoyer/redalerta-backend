package redalerta_backend.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.util.Collection;

/**
 * Representacion de seguridad optimizada del usuario autenticado en Spring Security.
 * Contiene la instancia en memoria del usuario autenticado para evitar consultas redundantes a la base de datos
 * durante el ciclo de vida de la misma peticion HTTP.
 */
public record UsuarioPrincipal(
        Usuario usuario,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    public Long getId() {
        return usuario.getId();
    }

    public Rol getRol() {
        return usuario.getRol();
    }

    public String getDepartamento() {
        return usuario.getUbicacion() != null ? usuario.getUbicacion().departamento() : null;
    }

    public String getMunicipio() {
        return usuario.getUbicacion() != null ? usuario.getUbicacion().municipio() : null;
    }
}
