package redalerta_backend.modules.usuario.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import redalerta_backend.common.exception.BadRequestException;

@Embeddable
public record Documento(
        @Column(name = "documento_numero", nullable = false, length = 30)
        String numero,

        @Enumerated(EnumType.STRING)
        @Column(name = "documento_tipo", nullable = false, length = 30)
        TipoDeDocumento tipo
) {
    public Documento {
        if (numero == null || numero.isBlank()) {
            throw new BadRequestException("El número del documento no puede estar vacío");
        }
        numero = numero.trim();
        if (tipo == null) {
            throw new BadRequestException("El tipo del documento no puede estar vacío");
        }
        if (!tipo.equals(TipoDeDocumento.PASAPORTE) && !numero.matches("[0-9]+")) {
            throw new BadRequestException("El número de documento no puede tener letras");
        }
    }
}