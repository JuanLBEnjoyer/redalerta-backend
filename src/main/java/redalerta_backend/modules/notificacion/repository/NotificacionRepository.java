package redalerta_backend.modules.notificacion.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.notificacion.entity.Notificacion;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.util.Optional;

/**
 * Repositorio JPA para la consulta y persistencia de notificaciones de la bandeja de entrada.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Obtiene el listado paginado de notificaciones de un usuario ordenadas por fecha descendente.
     */
    Page<Notificacion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario, Pageable pageable);

    /**
     * Obtiene el listado paginado de notificaciones filtradas por estado de lectura.
     */
    Page<Notificacion> findByUsuarioAndLeidaOrderByFechaCreacionDesc(Usuario usuario, Boolean leida, Pageable pageable);

    /**
     * Busca una notificacion por su ID asegurando que pertenezca al usuario consultante (proteccion BOLA/IDOR).
     */
    Optional<Notificacion> findByIdAndUsuario(Long id, Usuario usuario);

    /**
     * Cuenta cuantas notificaciones no leidas tiene un usuario (util para badges en el cliente).
     */
    long countByUsuarioAndLeidaFalse(Usuario usuario);

    /**
     * Marca todas las notificaciones pendientes de un usuario como leidas en una sola consulta atomica.
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario = :usuario AND n.leida = false")
    void marcarTodasComoLeidas(@Param("usuario") Usuario usuario);
}

