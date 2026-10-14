package redalerta_backend.modules.alerta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.alerta.entity.Alerta;

/**
 * Repositorio Spring Data JPA para la persistencia y consulta de {@link Alerta}.
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /**
     * Consulta el listado paginado de alertas creadas por un usuario especifico ordenadas descendentemente por fecha.
     *
     * @param autorId Identificador del usuario autor.
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas del autor.
     */
    Page<Alerta> findByAutorIdOrderByFechaCreacionDesc(Long autorId, Pageable pageable);
}
