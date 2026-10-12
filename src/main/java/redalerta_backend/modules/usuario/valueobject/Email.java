package redalerta_backend.modules.usuario.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import redalerta_backend.common.exception.BadRequestException;

@Embeddable
public record Email(
        @Column(name = "email", nullable = false, unique = true, length = 150)
        String valor
) {
    public Email {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El email no puede ser nulo o vacío");
        }
        valor = valor.trim().toLowerCase();
        if (!valor.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("El email no tiene un formato válido");
        }
    }
}
