package redalerta_backend.modules.alerta.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import redalerta_backend.common.exception.BadRequestException;

import redalerta_backend.modules.catalogo.domain.CatalogoGeografico;

/**
 * Value Object inmutable que encapsula la ubicacion geografica normalizada de
 * una alerta.
 * Aplica principios de Domain-Driven Design (DDD) garantizando consistencia
 * en departamentos, municipios y la direccion detallada.
 */
@Embeddable
public record UbicacionAlerta(
        @Column(name = "departamento", nullable = false, length = 100) String departamento,

        @Column(name = "municipio", nullable = false, length = 100) String municipio,

        @Column(name = "direccion_detallada", nullable = false, length = 255) String direccionDetallada) {
    public UbicacionAlerta {
        if (departamento == null || departamento.isBlank()) {
            throw new BadRequestException("El departamento es obligatorio");
        }
        if (municipio == null || municipio.isBlank()) {
            throw new BadRequestException("El municipio es obligatorio");
        }
        if (direccionDetallada == null || direccionDetallada.isBlank()) {
            throw new BadRequestException("La direccion detallada o punto de referencia es obligatoria");
        }
        departamento = CatalogoGeografico.canonicalizarDepartamento(departamento);
        municipio = CatalogoGeografico.canonicalizarMunicipio(departamento, municipio);
        direccionDetallada = direccionDetallada.trim();
    }
}
