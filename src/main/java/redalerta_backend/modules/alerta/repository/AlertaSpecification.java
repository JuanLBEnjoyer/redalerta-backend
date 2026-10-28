package redalerta_backend.modules.alerta.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Especificacion JPA con criterios dinamicos para la consulta y filtrado de alertas comunitarias.
 */
public final class AlertaSpecification {

    public static final List<EstadoAlerta> ESTADOS_ACTIVOS = List.of(
            EstadoAlerta.PENDIENTE_REVISION,
            EstadoAlerta.DIRECCIONADA,
            EstadoAlerta.EN_GESTION
    );

    private AlertaSpecification() {
    }

    /**
     * Construye una {@link Specification} dinamica a partir de los criterios de filtrado provistos.
     *
     * @param filtro Criterios de filtrado por ubicacion, estado y rango de fechas.
     * @return Especificacion lista para ser ejecutada por el repositorio JPA.
     */
    public static Specification<Alerta> conFiltros(FiltroAlertaDto filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null) {
                // Filtro por departamento territorial (case-insensitive y canonicalizado)
                if (filtro.getDepartamento() != null && !filtro.getDepartamento().isBlank()) {
                    String canonicoDepto = redalerta_backend.modules.catalogo.domain.CatalogoGeografico.canonicalizarDepartamento(filtro.getDepartamento());
                    predicates.add(cb.equal(
                            cb.lower(root.get("ubicacion").get("departamento")),
                            canonicoDepto.trim().toLowerCase()
                    ));
                }

                // Filtro por municipio territorial (case-insensitive y canonicalizado)
                if (filtro.getMunicipio() != null && !filtro.getMunicipio().isBlank()) {
                    String canonicoMuni = redalerta_backend.modules.catalogo.domain.CatalogoGeografico.canonicalizarMunicipio(
                            filtro.getDepartamento(),
                            filtro.getMunicipio()
                    );
                    predicates.add(cb.equal(
                            cb.lower(root.get("ubicacion").get("municipio")),
                            canonicoMuni.trim().toLowerCase()
                    ));
                }

                // Filtro por estado puntual o estados activos por defecto
                if (filtro.getEstado() != null) {
                    predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
                } else {
                    predicates.add(root.get("estado").in(ESTADOS_ACTIVOS));
                }

                // Filtro por fecha inicial (inclusive desde las 00:00:00)
                if (filtro.getFechaInicio() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("fechaCreacion"),
                            filtro.getFechaInicio().atStartOfDay()
                    ));
                }

                // Filtro por fecha final (inclusive hasta las 23:59:59.999999999)
                if (filtro.getFechaFin() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            root.get("fechaCreacion"),
                            filtro.getFechaFin().atTime(LocalTime.MAX)
                    ));
                }
            } else {
                predicates.add(root.get("estado").in(ESTADOS_ACTIVOS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
