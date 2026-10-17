package redalerta_backend.modules.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.admin.entity.SolicitudRegistro;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * Repositorio Spring Data JPA para la persistencia y consulta de {@link SolicitudRegistro}.
 */
@Repository
public interface SolicitudRegistroRepository extends JpaRepository<SolicitudRegistro, Long> {

    /**
     * Consulta el listado paginado de solicitudes filtradas por su estado.
     *
     * @param estado Estado de la solicitud (ej. PENDIENTE).
     * @param pageable Configuracion de paginacion y ordenamiento.
     * @return Pagina de solicitudes de registro coincidentes.
     */
    Page<SolicitudRegistro> findByEstado(EstadoSolicitud estado, Pageable pageable);

    /**
     * Consulta el listado paginado de solicitudes filtradas por su estado y el rol del usuario solicitante.
     *
     * @param estado Estado de la solicitud (ej. PENDIENTE).
     * @param rol Rol del usuario postulante (ROLE_LIDER o ROLE_ORGANIZACION).
     * @param pageable Configuracion de paginacion y ordenamiento.
     * @return Pagina de solicitudes de registro coincidentes.
     */
    Page<SolicitudRegistro> findByEstadoAndUsuarioRol(EstadoSolicitud estado, Rol rol, Pageable pageable);
}
