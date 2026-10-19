package redalerta_backend.modules.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.valueobject.Documento;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDocumento(Documento documento);

    java.util.List<Usuario> findByRolAndUbicacionDepartamentoAndUbicacionMunicipioAndActivoTrue(
            redalerta_backend.modules.usuario.valueobject.Rol rol,
            String departamento,
            String municipio
    );

    java.util.List<Usuario> findByRolAndUbicacionDepartamentoAndActivoTrue(
            redalerta_backend.modules.usuario.valueobject.Rol rol,
            String departamento
    );
}
