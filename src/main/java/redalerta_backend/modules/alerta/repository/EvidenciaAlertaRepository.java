package redalerta_backend.modules.alerta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link EvidenciaAlerta}.
 */
@Repository
public interface EvidenciaAlertaRepository extends JpaRepository<EvidenciaAlerta, Long> {

    /**
     * Busca una evidencia fotografica por su URL publica.
     *
     * @param url URL publica de la imagen en S3.
     * @return Evidencia si existe en la base de datos.
     */
    Optional<EvidenciaAlerta> findByUrl(String url);

    /**
     * Busca una evidencia fotografica por su clave de objeto S3.
     *
     * @param s3Key Clave de objeto en S3.
     * @return Evidencia si existe en la base de datos.
     */
    Optional<EvidenciaAlerta> findByS3Key(String s3Key);
}

