package redalerta_backend.modules.usuario.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import redalerta_backend.common.exception.BadRequestException;

import redalerta_backend.modules.catalogo.domain.CatalogoGeografico;

/**
 * Value Object inmutable que representa la ubicacion territorial y de residencia de un usuario.
 * Aplica principios de Domain-Driven Design (DDD).
 */
@Embeddable
public record Ubicacion(
        @Column(name = "departamento", nullable = false, length = 100)
        String departamento,

        @Column(name = "municipio", nullable = false, length = 100)
        String municipio,

        @Column(name = "direccion_residencia", nullable = false, length = 150)
        String direccionResidencia
) {
    public Ubicacion {
        if (departamento == null || departamento.isBlank()) {
            throw new BadRequestException("El departamento es obligatorio");
        }
        if (municipio == null || municipio.isBlank()) {
            throw new BadRequestException("El municipio es obligatorio");
        }
        if (direccionResidencia == null || direccionResidencia.isBlank()) {
            throw new BadRequestException("La direccion de residencia es obligatoria");
        }
        departamento = CatalogoGeografico.canonicalizarDepartamento(departamento);
        municipio = CatalogoGeografico.canonicalizarMunicipio(departamento, municipio);
        direccionResidencia = direccionResidencia.trim();
    }
}
