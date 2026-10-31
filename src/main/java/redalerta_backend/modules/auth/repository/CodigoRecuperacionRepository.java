package redalerta_backend.modules.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.auth.entity.CodigoRecuperacion;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestion y consulta de codigos de recuperacion de contrasena.
 */
@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {

    /**
     * Obtiene el codigo de recuperacion activo mas reciente generado para un usuario.
     *
     * @param usuario Usuario destinatario.
     * @return Codigo de recuperacion no usado mas reciente, si existe.
     */
    Optional<CodigoRecuperacion> findTopByUsuarioAndUsadoFalseOrderByFechaCreacionDesc(Usuario usuario);

    /**
     * Obtiene todos los codigos activos no usados asociados a un usuario para invalidacion previa.
     *
     * @param usuario Usuario destinatario.
     * @return Lista de codigos no usados pendientes.
     */
    List<CodigoRecuperacion> findByUsuarioAndUsadoFalse(Usuario usuario);
}

