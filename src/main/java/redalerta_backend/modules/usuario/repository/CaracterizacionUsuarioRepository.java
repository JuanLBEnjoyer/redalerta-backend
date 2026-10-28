package redalerta_backend.modules.usuario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.usuario.entity.CaracterizacionUsuario;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la gestion de encuestas de caracterizacion social.
 * Mantiene la persistencia desacoplada de la entidad Usuario para evitar sobrecarga (over-fetching).
 */
@Repository
public interface CaracterizacionUsuarioRepository extends JpaRepository<CaracterizacionUsuario, Long> {

    /**
     * Consulta la caracterizacion demografica y social asociada a un usuario especifico.
     *
     * @param usuarioId Identificador unico del usuario.
     * @return Encuesta de caracterizacion encontrada o vacio si no existe.
     */
    Optional<CaracterizacionUsuario> findByUsuarioId(Long usuarioId);

    /**
     * Elimina el registro de caracterizacion perteneciente a un usuario.
     *
     * @param usuarioId Identificador unico del usuario.
     */
    void deleteByUsuarioId(Long usuarioId);
}
