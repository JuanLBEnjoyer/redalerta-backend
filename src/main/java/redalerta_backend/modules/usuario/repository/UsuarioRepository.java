package redalerta_backend.modules.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Email;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(Email email);

    boolean existsByEmail(Email email);

    boolean existsByDocumento(Documento documento);
}
