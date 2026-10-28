package redalerta_backend.modules.usuario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import redalerta_backend.modules.usuario.valueobject.Genero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "caracterizaciones_usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaracterizacionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enGestacionLactancia = false;

    @Column(nullable = false)
    private Integer numeroIntegrantesFamilia;

    @Builder.Default
    @Column(nullable = false)
    private Boolean tieneMenoresDeEdad = false;

    @Builder.Default
    private Integer cantidadMenores = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean tieneMiembroDiscapacidad = false;

    @Column(length = 200)
    private String especificacionDiscapacidad;

    @Builder.Default
    @Column(nullable = false)
    private Boolean perteneceGrupoEtnico = false;

    @Column(length = 100)
    private String especificacionGrupoEtnico;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEncuesta = LocalDateTime.now();

    public Integer getEdadCalculada() {
        if (this.fechaNacimiento == null) {
            return null;
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }
}