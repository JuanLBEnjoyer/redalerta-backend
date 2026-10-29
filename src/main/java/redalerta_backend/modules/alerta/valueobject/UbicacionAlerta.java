package redalerta_backend.modules.alerta.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import redalerta_backend.common.exception.BadRequestException;

/**
 * Value Object inmutable que encapsula la ubicacion geografica y coordenadas
 * geodesicas GPS de una alerta comunitaria.
 * Aplica principios de Domain-Driven Design (DDD) garantizando consistencia
 * en departamentos, municipios, direccion y limites geodesicos WGS84.
 */
@Embeddable
public record UbicacionAlerta(
        @Column(name = "departamento", nullable = false, length = 100)
        String departamento,

        @Column(name = "municipio", nullable = false, length = 100)
        String municipio,

        @Column(name = "direccion_detallada", nullable = false, length = 255)
        String direccionDetallada,

        @Column(name = "latitud")
        Double latitud,

        @Column(name = "longitud")
        Double longitud,

        @Column(name = "altitud")
        Double altitud
) {
    public UbicacionAlerta {
        if (departamento == null || departamento.isBlank()) {
            throw new BadRequestException("El departamento es obligatorio");
        }
        if (municipio == null || municipio.isBlank()) {
            throw new BadRequestException("El municipio es obligatorio");
        }
        if (latitud == null || latitud < -90.0 || latitud > 90.0) {
            throw new BadRequestException("La latitud es obligatoria y debe estar comprendida entre -90.0 y 90.0 grados");
        }
        if (longitud == null || longitud < -180.0 || longitud > 180.0) {
            throw new BadRequestException("La longitud es obligatoria y debe estar comprendida entre -180.0 y 180.0 grados");
        }
        if (altitud != null && (altitud < -500.0 || altitud > 9000.0)) {
            throw new BadRequestException("La altitud debe estar comprendida entre -500.0 y 9000.0 metros");
        }
        if (direccionDetallada == null || direccionDetallada.isBlank()) {
            direccionDetallada = "Ubicacion capturada por GPS";
        } else {
            direccionDetallada = direccionDetallada.trim();
        }
        departamento = departamento.trim();
        municipio = municipio.trim();
    }
}
