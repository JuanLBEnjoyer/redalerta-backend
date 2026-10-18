package redalerta_backend.modules.alerta.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
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
import redalerta_backend.modules.alerta.valueobject.CategoriaAlerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.PrioridadAlerta;
import redalerta_backend.modules.alerta.valueobject.RegistroHistorial;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una alerta comunitaria creada por un ciudadano.
 */
@Entity
@Table(name = "alertas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Embedded
    private UbicacionAlerta ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario autor;

    @Column(name = "es_anonimo", nullable = false)
    @Builder.Default
    private Boolean esAnonimo = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoAlerta estado = EstadoAlerta.PENDIENTE_REVISION;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CategoriaAlerta categoria;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PrioridadAlerta prioridad;

    @OneToMany(mappedBy = "alerta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EvidenciaAlerta> evidencias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "alertas_historial", joinColumns = @JoinColumn(name = "alerta_id"))
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RegistroHistorial> historial = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public void agregarEvidencia(EvidenciaAlerta evidencia) {
        if (this.evidencias == null) {
            this.evidencias = new ArrayList<>();
        }
        this.evidencias.add(evidencia);
        evidencia.setAlerta(this);
    }

    public void registrarPasoHistorial(String motivo, EstadoAlerta nuevoEstado, Usuario responsable) {
        if (this.historial == null) {
            this.historial = new ArrayList<>();
        }
        this.estado = nuevoEstado;
        this.historial.add(new RegistroHistorial(
                motivo,
                LocalDateTime.now(),
                nuevoEstado,
                responsable.getId(),
                responsable.getNombre(),
                responsable.getRol() != null ? responsable.getRol().name() : "DESCONOCIDO"
        ));
    }
}
