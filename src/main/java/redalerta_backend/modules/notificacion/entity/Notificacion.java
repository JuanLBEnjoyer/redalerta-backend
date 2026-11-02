package redalerta_backend.modules.notificacion.entity;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.time.LocalDateTime;

/**
 * Entidad que representa una notificacion dirigida a un usuario en su bandeja de entrada.
 * Contiene el resumen visual de la accion y el detalle explicativo del cambio de estado.
 */
@Entity
@Table(name = "notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Alerta alerta;

    @Column(name = "alerta_id_referencia", nullable = false)
    private Long alertaId;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "nuevo_estado", nullable = false, length = 30)
    private EstadoAlerta nuevoEstado;

    @Column(name = "motivo_accion", length = 1000)
    private String motivoAccion;

    @Column(name = "responsable_nombre", nullable = false, length = 100)
    private String responsableNombre;

    @Column(name = "responsable_rol", nullable = false, length = 50)
    private String responsableRol;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}

