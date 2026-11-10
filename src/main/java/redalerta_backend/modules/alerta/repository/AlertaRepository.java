package redalerta_backend.modules.alerta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.util.Collection;

/**
 * Repositorio Spring Data JPA para la persistencia y consulta de {@link Alerta}.
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long>, JpaSpecificationExecutor<Alerta> {

    /**
     * Consulta el listado paginado de alertas creadas por un usuario especifico ordenadas descendentemente por fecha.
     *
     * @param autorId Identificador del usuario autor.
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas del autor.
     */
    Page<Alerta> findByAutorIdOrderByFechaCreacionDesc(Long autorId, Pageable pageable);

    /**
     * Consulta el listado paginado de alertas filtradas por una coleccion de estados, ordenadas descendentemente por fecha.
     *
     * @param estados Coleccion de estados a incluir en la consulta.
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas coincidentes.
     */
    Page<Alerta> findByEstadoInOrderByFechaCreacionDesc(Collection<EstadoAlerta> estados, Pageable pageable);

    /**
     * Consulta el listado paginado de alertas filtradas por un estado especifico, ordenadas descendentemente por fecha.
     *
     * @param estado Estado especifico de la alerta.
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas coincidentes.
     */
    Page<Alerta> findByEstadoOrderByFechaCreacionDesc(EstadoAlerta estado, Pageable pageable);

    /**
     * Consulta el listado paginado de alertas direccionadas por un lider comunitario especifico.
     *
     * @param liderId Identificador unico del lider.
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas direccionadas.
     */
    Page<Alerta> findByLiderComunitarioIdOrderByFechaCreacionDesc(Long liderId, Pageable pageable);

    /**
     * Consulta la bandeja de alertas asignadas a una organizacion especifica en una coleccion de estados.
     *
     * @param organizacionId Identificador unico de la entidad u organizacion.
     * @param estados Estados de atencion esperados (ej. DIRECCIONADA, EN_GESTION).
     * @param pageable Configuracion de paginacion.
     * @return Pagina de alertas en bandeja de entrada de la organizacion.
     */
    Page<Alerta> findByOrganizacionAsignadaIdAndEstadoInOrderByFechaCreacionDesc(
            Long organizacionId,
            Collection<EstadoAlerta> estados,
            Pageable pageable
    );
}
