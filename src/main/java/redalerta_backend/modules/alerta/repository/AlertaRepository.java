package redalerta_backend.modules.alerta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.alerta.entity.Alerta;

/**
 * Repositorio Spring Data JPA para la persistencia de {@link Alerta}.
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
}
