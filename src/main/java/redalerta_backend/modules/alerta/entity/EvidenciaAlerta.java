package redalerta_backend.modules.alerta.entity;

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
 * Entidad que almacena los metadatos de una evidencia multimedia vinculada a
 * una alerta.
 * Los binarios reales se almacenan en un bucket de objetos (AWS S3).
 */
@Entity
@Table(name = "evidencias_alerta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Alerta alerta;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String s3Key;

    @Column(length = 50)
    private String tipoMime;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
