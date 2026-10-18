package redalerta_backend.modules.alerta;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.repository.AlertaSpecification;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaSpecificationTest {

    @Mock
    private Root<Alerta> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> ubicacionPath;

    @Mock
    private Path<String> departamentoPath;

    @Mock
    private Path<String> municipioPath;

    @Mock
    private Path<EstadoAlerta> estadoPath;

    @Mock
    private Path<LocalDateTime> fechaCreacionPath;

    @Mock
    private Predicate predicateMock;

    @Test
    @DisplayName("Debe construir predicados cuando el filtro es nulo usando estados activos por defecto")
    void conFiltrosNulo() {
        when(root.<EstadoAlerta>get("estado")).thenReturn(estadoPath);
        when(estadoPath.in(AlertaSpecification.ESTADOS_ACTIVOS)).thenReturn(predicateMock);
        when(cb.and(any(Predicate[].class))).thenReturn(predicateMock);

        Specification<Alerta> spec = AlertaSpecification.conFiltros(null);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(root).get("estado");
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Debe construir predicados territoriales con departamento y municipio")
    void conFiltrosTerritoriales() {
        FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                .departamento("Quindio")
                .municipio("Armenia")
                .build();

        when(root.get("ubicacion")).thenReturn(ubicacionPath);
        when(ubicacionPath.<String>get("departamento")).thenReturn(departamentoPath);
        when(ubicacionPath.<String>get("municipio")).thenReturn(municipioPath);
        when(cb.lower(departamentoPath)).thenReturn(departamentoPath);
        when(cb.lower(municipioPath)).thenReturn(municipioPath);
        when(cb.equal(departamentoPath, "quindío")).thenReturn(predicateMock);
        when(cb.equal(municipioPath, "armenia")).thenReturn(predicateMock);

        when(root.<EstadoAlerta>get("estado")).thenReturn(estadoPath);
        when(estadoPath.in(AlertaSpecification.ESTADOS_ACTIVOS)).thenReturn(predicateMock);
        when(cb.and(any(Predicate[].class))).thenReturn(predicateMock);

        Specification<Alerta> spec = AlertaSpecification.conFiltros(filtro);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(departamentoPath, "quindío");
        verify(cb).equal(municipioPath, "armenia");
    }

    @Test
    @DisplayName("Debe construir predicado con estado especifico cuando se proporciona")
    void conFiltroEstadoEspecifico() {
        FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                .estado(EstadoAlerta.RESUELTA)
                .build();

        when(root.<EstadoAlerta>get("estado")).thenReturn(estadoPath);
        when(cb.equal(estadoPath, EstadoAlerta.RESUELTA)).thenReturn(predicateMock);
        when(cb.and(any(Predicate[].class))).thenReturn(predicateMock);

        Specification<Alerta> spec = AlertaSpecification.conFiltros(filtro);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(estadoPath, EstadoAlerta.RESUELTA);
    }

    @Test
    @DisplayName("Debe construir predicados de rango de fechas con fechaInicio y fechaFin")
    void conFiltrosDeFechas() {
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fin = LocalDate.of(2026, 10, 15);

        FiltroAlertaDto filtro = FiltroAlertaDto.builder()
                .fechaInicio(inicio)
                .fechaFin(fin)
                .build();

        when(root.<EstadoAlerta>get("estado")).thenReturn(estadoPath);
        when(estadoPath.in(AlertaSpecification.ESTADOS_ACTIVOS)).thenReturn(predicateMock);

        when(root.<LocalDateTime>get("fechaCreacion")).thenReturn(fechaCreacionPath);
        when(cb.greaterThanOrEqualTo(eq(fechaCreacionPath), eq(inicio.atStartOfDay()))).thenReturn(predicateMock);
        when(cb.lessThanOrEqualTo(eq(fechaCreacionPath), eq(fin.atTime(LocalTime.MAX)))).thenReturn(predicateMock);
        when(cb.and(any(Predicate[].class))).thenReturn(predicateMock);

        Specification<Alerta> spec = AlertaSpecification.conFiltros(filtro);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).greaterThanOrEqualTo(fechaCreacionPath, inicio.atStartOfDay());
        verify(cb).lessThanOrEqualTo(fechaCreacionPath, fin.atTime(LocalTime.MAX));
    }
}
