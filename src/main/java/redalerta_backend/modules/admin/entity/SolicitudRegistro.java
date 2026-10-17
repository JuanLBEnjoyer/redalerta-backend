package redalerta_backend.modules.admin.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una solicitud de registro o postulación institucional/comunitaria
 * sujeta a revisión administrativa (proceso de Vetting).
 */
@Entity
@Table(name = "solicitudes_registro")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(length = 150)
    private String sectorComunidad;

    @Column(length = 150)
    private String razonSocial;

    @Column(length = 50)
    private String nit;

    @Column(length = 1000)
    private String observacionesAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario revisadoPor;

    private LocalDateTime fechaRevision;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPostulacion = LocalDateTime.now();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EvidenciaSolicitud> evidencias = new ArrayList<>();

    public void agregarEvidencia(EvidenciaSolicitud evidencia) {
        if (this.evidencias == null) {
            this.evidencias = new ArrayList<>();
        }
        this.evidencias.add(evidencia);
        evidencia.setSolicitud(this);
    }
}
