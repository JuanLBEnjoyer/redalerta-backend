package redalerta_backend.modules.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad que representa un soporte documental o evidencia adjunta a una postulación de registro.
 */
@Entity
@Table(name = "evidencias_solicitudes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 200)
    private String nombreArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SolicitudRegistro solicitud;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
